package com.runanywhere.startup_hackathon.medicap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.ExpiryEntity
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpiryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).expiryDao() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("2026-12-31") }
    var notes by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color.White
        )
    )

    Scaffold(
        topBar = { MediCapTopBar(title = "Add Expiry", onBack = onBack) },
        containerColor = ScreenBg
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(gradient)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ✅ Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Medicine expiry details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )
                    Text(
                        text = "Save expiry dates to get reminders before medicines expire.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            // ✅ Input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine name") },
                        placeholder = { Text("e.g. Paracetamol 500mg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("Expiry date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        supportingText = {
                            Text(
                                "Format: year-month-day",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("Strip opened / keep refrigerated / dosage…") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 2
                    )
                }
            }

            // ✅ Save button
            Button(
                onClick = {
                    if (name.trim().isBlank()) return@Button
                    saving = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            dao.insert(
                                ExpiryEntity(
                                    id = 0,
                                    medicineName = name.trim(),
                                    expiryDate = expiry.trim(),
                                    notes = notes.trim().ifBlank { null }
                                )
                            )
                        }
                        saving = false
                        onSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !saving,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Color.White
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(0.dp))
                    Text(" Saving…")
                } else {
                    Text(
                        "Save Expiry",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // ✅ Disclaimer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = "Tip: Add expiry dates as soon as you buy medicines to avoid last-minute risks.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
        }
    }
}
