package com.runanywhere.startup_hackathon.medicap.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.HealthProfileEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicalRecordEntity
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import com.runanywhere.startup_hackathon.medicap.ui.health.HealthRecordsVMFactory
import com.runanywhere.startup_hackathon.medicap.ui.health.HealthRecordsViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import androidx.core.content.FileProvider

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val healthDao = remember { AppDatabase.get(context).healthDao() }
    val vm: HealthRecordsViewModel = viewModel(factory = HealthRecordsVMFactory(healthDao))

    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { MediCapTopBar(title = "Health Records", onBack = onBack) },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ✅ Premium + safe (no tabIndicatorOffset)
            TabRow(
                selectedTabIndex = tab,
                containerColor = Color.White,
                contentColor = Ink
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Profile", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Filled.Person, null) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Documents", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Filled.Folder, null) }
                )
                Tab(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    text = { Text("Emergency", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Filled.Warning, null) }
                )
            }

            when (tab) {
                0 -> ProfileTab(vm)
                1 -> DocumentsTab(vm)
                2 -> EmergencyTab(vm)
            }
        }
    }
}


/* -------------------------------- PROFILE TAB (FIXED) -------------------------------- */

@Composable
private fun ProfileTab(vm: HealthRecordsViewModel) {
    val p by vm.profile.collectAsState()

    var name by remember(p.name) { mutableStateOf(p.name) }
    var age by remember(p.age) { mutableStateOf(if (p.age <= 0) "" else p.age.toString()) }
    var gender by remember(p.gender) { mutableStateOf(p.gender) }
    var blood by remember(p.bloodGroup) { mutableStateOf(p.bloodGroup) }
    var allergies by remember(p.allergies) { mutableStateOf(p.allergies) }
    var conditions by remember(p.conditions) { mutableStateOf(p.conditions) }
    var emergencyContact by remember(p.emergencyContact) { mutableStateOf(p.emergencyContact) }

    val listState = rememberLazyListState()

    // ✅ Gradient background for premium feel
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // ✅ Scrollable form
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ProfileHeaderCard()
            }

            item {
                SectionTitle("Basic info")
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Person, null) }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter(Char::isDigit).take(3) },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Cake, null) }
                    )

                    OutlinedTextField(
                        value = blood,
                        onValueChange = { blood = it },
                        label = { Text("Blood group") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Bloodtype, null) }
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Wc, null) }
                )
            }

            item {
                SectionTitle("Medical details")
            }

            item {
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    leadingIcon = { Icon(Icons.Filled.Healing, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = conditions,
                    onValueChange = { conditions = it },
                    label = { Text("Conditions (BP/Diabetes/Asthma etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    leadingIcon = { Icon(Icons.Filled.MedicalServices, null) }
                )
            }

            item {
                SectionTitle("Emergency contact")
            }

            item {
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency contact") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Call, null) }
                )
            }

            item {
                SupportNoteCard()
            }
        }

        // ✅ Sticky bottom Save bar (always visible)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Save changes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Your data stays on device (offline-first).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        vm.saveProfile(
                            HealthProfileEntity(
                                id = 1,
                                name = name.trim(),
                                age = age.toIntOrNull() ?: 0,
                                gender = gender.trim(),
                                bloodGroup = blood.trim(),
                                allergies = allergies.trim(),
                                conditions = conditions.trim(),
                                emergencyContact = emergencyContact.trim()
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Badge, contentDescription = null, tint = Color(0xFF6366F1))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Health Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Fill once, use anywhere in MediCap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Ink,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SupportNoteCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Ink)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Privacy-first",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Profile stays on your device unless you share it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
        }
    }
}

/* -------------------------------- DOCUMENTS TAB (PREMIUM + FAB) -------------------------------- */

@Composable
private fun DocumentsTab(vm: HealthRecordsViewModel) {
    val context = LocalContext.current
    val records by vm.records.collectAsState()

    var showAdd by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ✅ cleaner header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Your documents",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Prescriptions, lab reports, scans",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (records.isEmpty()) {
                EmptyDocs()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { r ->
                        RecordCard(
                            record = r,
                            onOpen = {
                                val uri = r.uriString.toUri()
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching { context.startActivity(intent) }
                            },
                            onShare = {
                                val uri = r.uriString.toUri()
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = context.contentResolver.getType(uri) ?: "*/*"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${r.type}: ${r.title}\nDate: ${r.date}\nNotes: ${r.notes ?: "—"}"
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(share, "Share record via"))
                            },
                            onDelete = { vm.deleteRecord(r) }
                        )
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        // ✅ Real app FAB for Add
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Ink,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add record")
        }

        if (showAdd) {
            AddRecordDialog(
                onDismiss = { showAdd = false },
                onAdd = { type, title, doctor, notes, uriStr ->
                    vm.addRecord(
                        MedicalRecordEntity(
                            type = type,
                            title = title,
                            date = LocalDate.now().toString(),
                            doctorOrHospital = doctor.ifBlank { null },
                            notes = notes.ifBlank { null },
                            uriString = uriStr
                        )
                    )
                    showAdd = false
                }
            )
        }
    }
}

/* -------------------------------- EMERGENCY TAB (SCROLL + PREMIUM) -------------------------------- */

@Composable
private fun EmergencyTab(vm: HealthRecordsViewModel) {
    val context = LocalContext.current
    val profile by vm.profile.collectAsState()

    val summary = remember(profile) { vm.buildEmergencySummary() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFBE123C))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Emergency Quick Share",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9F1239),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Share summary with doctor instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9F1239)
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = summary,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                minLines = 10,
                label = { Text("Summary") }
            )
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "MediCap Emergency Summary")
                        putExtra(Intent.EXTRA_TEXT, summary)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Share, null)
                Spacer(Modifier.width(8.dp))
                Text("Share Summary")
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val dial = Intent(Intent.ACTION_DIAL).apply { data = "tel:".toUri() }
                    runCatching { context.startActivity(dial) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Call, null)
                Spacer(Modifier.width(8.dp))
                Text("Call Doctor (Dial)")
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }
}

/* -------------------------------- UI HELPERS (SAME LOGIC) -------------------------------- */

@Composable
private fun EmptyDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(34.dp), tint = Ink)
        }
        Spacer(Modifier.height(10.dp))
        Text("No records yet", fontWeight = FontWeight.SemiBold, color = Ink)
        Text(
            "Tap + to upload prescriptions, lab reports, or scans.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun RecordCard(
    record: MedicalRecordEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Description, null, tint = Ink)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        record.title,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${record.type} • ${record.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Open", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Share", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/* -------------------------------- ADD RECORD DIALOG (SAME LOGIC, BETTER SPACING) -------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecordDialog(
    onDismiss: () -> Unit,
    onAdd: (type: String, title: String, doctor: String, notes: String, uriStr: String) -> Unit
) {
    val context = LocalContext.current

    var type by remember { mutableStateOf("Prescription") }
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedUri = uri
    }

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) selectedUri = saveBitmapToCacheAndGetUri(context, bmp)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedUri
                    if (title.trim().isNotEmpty() && uri != null) {
                        onAdd(type, title.trim(), doctor.trim(), notes.trim(), uri.toString())
                    }
                },
                enabled = title.trim().isNotEmpty() && selectedUri != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add medical record", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                val types = listOf("Prescription", "Lab Report", "Scan", "Discharge", "Vaccination", "Other")
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = { type = t; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor / Hospital (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { pickFile.launch("*/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.UploadFile, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Upload", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(onClick = { takePhoto.launch(null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.CameraAlt, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Camera", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Text(
                    text = if (selectedUri == null) "No file selected" else "Selected: ${selectedUri.toString().take(52)}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
        }
    )
}

private fun saveBitmapToCacheAndGetUri(context: android.content.Context, bmp: Bitmap): Uri? {
    return runCatching {
        val file = File(context.cacheDir, "medicap_record_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }.getOrNull()
}
