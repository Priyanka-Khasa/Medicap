package com.runanywhere.startup_hackathon.medicap.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onResultPick: (Long) -> Unit,
    onManualSearch: (() -> Unit)? = null,
    autoOpenBestMatch: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.get(context) }
    val daoAny = remember { db.medicineDao() as Any }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var isAnalyzing by remember { mutableStateOf(false) }
    var liveText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var results by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF)
        )
    )

    Scaffold(
        topBar = { MediCapTopBar(title = "Scan Medicine", onBack = onBack) },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(gradient)
        ) {

            // ✅ Premium camera card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                ) {
                    if (!hasPermission) {
                        PermissionBlock { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    } else {
                        LiveCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            lifecycleOwner = lifecycleOwner,
                            cameraExecutor = cameraExecutor,
                            onLiveText = { liveText = it },
                            onAnalyzerState = { isAnalyzing = it }
                        )
                    }

                    // ✅ Overlay scan frame
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(22.dp)
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                    )

                    // ✅ Live pill (no letter-by-letter overflow)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Filled.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            Text(
                                text = if (liveText.isBlank()) "Point camera at medicine name / strip" else liveText,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (liveText.isNotBlank()) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ✅ Status row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (searching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ✅ Bottom action bar (real app)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val text = liveText.trim()
                                if (text.isBlank()) {
                                    status = "No text detected. Try better lighting."
                                    results = emptyList()
                                    return@Button
                                }

                                scope.launch {
                                    searching = true
                                    status = "Searching medicines…"
                                    val matches = withContext(Dispatchers.IO) {
                                        findMatchesFromText(daoAny, text)
                                    }
                                    results = matches
                                    status = if (matches.isEmpty()) "No matches found" else "Matches found: ${matches.size}"
                                    searching = false

                                    if (autoOpenBestMatch) {
                                        autoOpenIfConfident(text, matches, onResultPick)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            enabled = hasPermission && !isAnalyzing && !searching,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Now", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = {
                                liveText = ""
                                results = emptyList()
                                status = "Ready"
                            },
                            modifier = Modifier.width(120.dp).height(54.dp),
                            enabled = !searching,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Reset", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (onManualSearch != null) {
                        OutlinedButton(
                            onClick = onManualSearch,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Search manually", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ✅ Results area
            when {
                results.isEmpty() -> {
                    EmptyScanState(
                        title = "No results yet",
                        subtitle = "Scan a medicine strip/box. Then MediCap will show best matches from your offline database."
                    )
                }
                else -> {
                    Text(
                        text = "Matches",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results, key = { it.id }) { med ->
                            MedicineResultCardPro(med) { onResultPick(med.id) }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Analyzer (NO ExperimentalGetImage) ---------------- */

private class OcrAnalyzerNoExperimental(
    private val recognizer: TextRecognizer,
    private val onLiveText: (String) -> Unit,
    private val onAnalyzerState: (Boolean) -> Unit,
    private val throttleMs: Long = 650L
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedAt: Long = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalyzedAt < throttleMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedAt = now

        try {
            if (imageProxy.format != ImageFormat.YUV_420_888) {
                imageProxy.close()
                return
            }

            onAnalyzerState(true)

            val nv21 = yuv420888ToNv21(imageProxy)
            val rotation = imageProxy.imageInfo.rotationDegrees

            val inputImage = InputImage.fromByteArray(
                nv21,
                imageProxy.width,
                imageProxy.height,
                rotation,
                InputImage.IMAGE_FORMAT_NV21
            )

            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    val cleaned = result.text
                        .replace(Regex("[^A-Za-z0-9+\\-\\s]"), " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    onLiveText(cleaned.take(180))
                }
                .addOnCompleteListener {
                    onAnalyzerState(false)
                    imageProxy.close()
                }
        } catch (_: Throwable) {
            onAnalyzerState(false)
            imageProxy.close()
        }
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val width = image.width
        val height = image.height

        val ySize = width * height
        val uvSize = width * height / 2
        val out = ByteArray(ySize + uvSize)

        copyPlane(
            buffer = yPlane.buffer,
            rowStride = yPlane.rowStride,
            pixelStride = yPlane.pixelStride,
            width = width,
            height = height,
            out = out,
            outOffset = 0
        )

        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val chromaWidth = width / 2
        val chromaHeight = height / 2
        var outIndex = ySize

        val uRowStride = uPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        val uBase = uBuffer.position()
        val vBase = vBuffer.position()

        for (row in 0 until chromaHeight) {
            val uRowStart = uBase + row * uRowStride
            val vRowStart = vBase + row * vRowStride
            for (col in 0 until chromaWidth) {
                val uIndex = uRowStart + col * uPixelStride
                val vIndex = vRowStart + col * vPixelStride
                out[outIndex++] = vBuffer.get(vIndex) // V
                out[outIndex++] = uBuffer.get(uIndex) // U
            }
        }

        return out
    }

    private fun copyPlane(
        buffer: ByteBuffer,
        rowStride: Int,
        pixelStride: Int,
        width: Int,
        height: Int,
        out: ByteArray,
        outOffset: Int
    ) {
        val baseOffset = buffer.position()
        var outIndex = outOffset

        for (row in 0 until height) {
            val rowStart = baseOffset + row * rowStride
            var colIndex = rowStart
            for (col in 0 until width) {
                out[outIndex++] = buffer.get(colIndex)
                colIndex += pixelStride
            }
        }
    }
}

/* ---------------- Camera Preview ---------------- */

@Composable
private fun LiveCameraPreview(
    modifier: Modifier,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onLiveText: (String) -> Unit,
    onAnalyzerState: (Boolean) -> Unit
) {
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(Unit) { onDispose { runCatching { recognizer.close() } } }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(
                    cameraExecutor,
                    OcrAnalyzerNoExperimental(
                        recognizer = recognizer,
                        onLiveText = onLiveText,
                        onAnalyzerState = onAnalyzerState
                    )
                )

                runCatching { cameraProvider.unbindAll() }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/* ---------------- UI Blocks ---------------- */

@Composable
private fun PermissionBlock(onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(36.dp), tint = Color(0xFF6366F1))
                }

                Text(
                    "Camera permission required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Allow camera access to scan medicine packaging.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                ) {
                    Text("Grant permission")
                }
            }
        }
    }
}

@Composable
private fun EmptyScanState(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Medication, contentDescription = null, tint = Color(0xFF334155))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MedicineResultCardPro(
    medicine: MedicineEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Medication, null, tint = Ink, modifier = Modifier.size(26.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    medicine.display,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Code: ${medicine.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFEEF2FF)
                ) {
                    Text(
                        "Tap to open details",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF3730A3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF6366F1))
        }
    }
}

/* ---------------- Matching (UNCHANGED) ---------------- */

private fun findMatchesFromText(daoAny: Any, rawText: String): List<MedicineEntity> {
    val query = rawText.lowercase(Locale.getDefault())
        .replace(Regex("\\s+"), " ")
        .trim()

    if (query.isBlank()) return emptyList()

    val searchCandidates = listOf(
        "searchMedicines", "search", "searchByName", "searchByQuery", "searchByText",
        "findByName", "findByQuery"
    )

    for (name in searchCandidates) {
        val out = tryInvokeListString(daoAny, name, query)
        if (out != null) return out.sortedByDescending { scoreMatch(query, it) }.take(20)
    }

    val getAllCandidates = listOf("getAll", "getAllMedicines", "all", "getMedicines", "getAllItems")
    for (name in getAllCandidates) {
        val all = tryInvokeListNoArgs(daoAny, name)
        if (all != null) {
            return all.asSequence()
                .map { it to scoreMatch(query, it) }
                .filter { it.second > 0.0 }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(20)
                .toList()
        }
    }
    return emptyList()
}

private fun tryInvokeListString(daoAny: Any, methodName: String, arg: String): List<MedicineEntity>? {
    return try {
        val m: Method = daoAny.javaClass.methods.firstOrNull { method ->
            method.name == methodName &&
                    method.parameterTypes.size == 1 &&
                    method.parameterTypes[0] == String::class.java
        } ?: return null

        (m.invoke(daoAny, arg) as? List<*>)?.filterIsInstance<MedicineEntity>()
    } catch (_: Throwable) {
        null
    }
}

private fun tryInvokeListNoArgs(daoAny: Any, methodName: String): List<MedicineEntity>? {
    return try {
        val m: Method = daoAny.javaClass.methods.firstOrNull { method ->
            method.name == methodName && method.parameterTypes.isEmpty()
        } ?: return null

        (m.invoke(daoAny) as? List<*>)?.filterIsInstance<MedicineEntity>()
    } catch (_: Throwable) {
        null
    }
}

private fun autoOpenIfConfident(
    scannedText: String,
    matches: List<MedicineEntity>,
    onResultPick: (Long) -> Unit
) {
    if (matches.isEmpty()) return
    if (matches.size == 1) {
        onResultPick(matches[0].id)
        return
    }

    val q = scannedText.lowercase(Locale.getDefault()).trim()
    val best = matches[0]
    val second = matches.getOrNull(1)

    val bestScore = scoreMatch(q, best)
    val secondScore = second?.let { scoreMatch(q, it) } ?: 0.0

    if (bestScore >= 0.75 && bestScore - secondScore >= 0.15) {
        onResultPick(best.id)
    }
}

private fun scoreMatch(query: String, med: MedicineEntity): Double {
    val name = med.display.lowercase(Locale.getDefault())
    val code = med.code.lowercase(Locale.getDefault())

    val qTokens = query.split(" ").filter { it.length >= 2 }.toSet()
    val nameTokens = name.split(" ").filter { it.length >= 2 }.toSet()

    val tokenHit = if (qTokens.isEmpty()) 0.0 else {
        qTokens.intersect(nameTokens).size.toDouble() / qTokens.size.toDouble()
    }

    val containsBoost = when {
        name.contains(query) -> 0.35
        code.contains(query) -> 0.30
        else -> 0.0
    }

    val editSim = max(normalizedSimilarity(query, name), normalizedSimilarity(query, code))

    return ((0.45 * tokenHit) + (0.35 * editSim) + containsBoost).coerceIn(0.0, 1.0)
}

private fun normalizedSimilarity(a: String, b: String): Double {
    if (a.isBlank() || b.isBlank()) return 0.0
    val aa = a.take(80)
    val bb = b.take(80)
    val dist = levenshtein(aa, bb).toDouble()
    val denom = max(aa.length, bb.length).toDouble().coerceAtLeast(1.0)
    return (1.0 - (dist / denom)).coerceIn(0.0, 1.0)
}

private fun levenshtein(s1: String, s2: String): Int {
    val n = s1.length
    val m = s2.length
    if (n == 0) return m
    if (m == 0) return n

    val prev = IntArray(m + 1) { it }
    val cur = IntArray(m + 1)

    for (i in 1..n) {
        cur[0] = i
        val ca = s1[i - 1]
        for (j in 1..m) {
            val cost = if (ca == s2[j - 1]) 0 else 1
            cur[j] = min(min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
        }
        for (j in 0..m) prev[j] = cur[j]
    }
    return prev[m]
}
