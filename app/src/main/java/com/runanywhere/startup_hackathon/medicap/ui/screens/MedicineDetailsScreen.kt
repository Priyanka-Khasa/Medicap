package com.runanywhere.startup_hackathon.medicap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runanywhere.startup_hackathon.medicap.core.ai.MediCapAi
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import com.runanywhere.startup_hackathon20.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailsScreen(
    medicineId: Long,
    onBack: () -> Unit,
    onAddExpiry: (MedicineEntity) -> Unit,
    onOpenModels: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).medicineDao() }

    var med by remember { mutableStateOf<MedicineEntity?>(null) }

    // Reuse template VM only for model-loaded state
    val chatVm = remember { ChatViewModel() }
    val currentModelId by chatVm.currentModelId.collectAsState()

    var aiText by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(medicineId) {
        med = withContext(Dispatchers.IO) { dao.getById(medicineId) }
    }

    fun generateAiSummary(display: String) {
        if (currentModelId == null) {
            aiError = "No model loaded. Open Models, download and load a model first."
            return
        }
        aiText = ""
        aiError = null
        aiLoading = true

        scope.launch {
            runCatching {
                MediCapAi.generateSummaryStream(display).collect { token ->
                    aiText += token
                }
            }.onFailure { e ->
                aiError = e.message ?: "AI failed"
            }
            aiLoading = false
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF)
        )
    )

    Scaffold(
        topBar = { MediCapTopBar(title = "Medicine details", onBack = onBack) },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val item = med
            if (item == null) {
                LoadingDetailsSkeleton()
                return@Column
            }

            // ✅ Premium header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
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
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Medication,
                            contentDescription = null,
                            tint = Ink
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.display,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Code: ${item.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill("Offline DB")
                            Pill("AI summary available")
                        }
                    }
                }
            }

            InfoBlockPro("Name / formulation", item.display, leading = Icons.Filled.Medication)
            InfoBlockPro("Code", item.code, leading = Icons.Filled.Memory)

            // ✅ Primary CTA
            Button(
                onClick = { onAddExpiry(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                androidx.compose.material3.Icon(Icons.Filled.Inventory2, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Add to Expiry Vault", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // ✅ AI card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AI summary",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Offline model-based explanation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        ModelStatusChip(loaded = currentModelId != null,onClickNoModel = onOpenModels)
                    }

                    if (currentModelId == null) {
                        WarningCard(
                            text = "Model not loaded. Download + Load a model from Models to enable offline AI."
                        )

                        OutlinedButton(
                            onClick = onOpenModels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            androidx.compose.material3.Icon(Icons.Filled.Memory, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open Models", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Button(
                            onClick = { generateAiSummary(item.display) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !aiLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5),
                                contentColor = Color.White
                            )
                        ) {
                            if (aiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Generating…", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            } else {
                                androidx.compose.material3.Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(10.dp))
                                Text("Generate AI summary", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    aiError?.let {
                        ErrorCard(text = it)
                    }

                    if (aiText.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Text(
                                text = aiText,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink
                            )
                        }
                    }

                    // Disclaimer
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            "This is informational only, not medical advice.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBlockPro(
    title: String,
    body: String,
    leading: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(leading, contentDescription = null, tint = Ink)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFF1F5F9)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF334155),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ModelStatusChip(
    loaded: Boolean,
    onClickNoModel: (() -> Unit)? = null
) {
    val bg = if (loaded) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
    val fg = if (loaded) Color(0xFF166534) else Color(0xFF9F1239)
    val icon = if (loaded) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline
    val label = if (loaded) "Model loaded" else "No model • Tap"

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        onClick = {
            if (!loaded) onClickNoModel?.invoke()
        },
        enabled = !loaded && onClickNoModel != null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WarningCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBEB)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFD97706))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF92400E),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ErrorCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoadingDetailsSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}
