package com.runanywhere.startup_hackathon.medicap.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onBack: () -> Unit,
    doctorName: String = "My Doctor",
    doctorPhone: String = "",        // e.g. "9876543210"
    whatsappNumber: String = ""      // e.g. "919876543210" (with country code)
) {
    val context = LocalContext.current

    fun safeStart(intent: Intent) {
        runCatching { context.startActivity(intent) }
    }

    fun dial(number: String) {
        val n = number.trim()
        if (n.isBlank()) {
            safeStart(Intent(Intent.ACTION_DIAL))
            return
        }
        safeStart(
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$n")
            }
        )
    }

    fun openWhatsApp(numberWithCountry: String, message: String) {
        val n = numberWithCountry.trim()
        if (n.isBlank()) return
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val uri = Uri.parse("https://wa.me/$n?text=$encoded")
        safeStart(Intent(Intent.ACTION_VIEW, uri))
    }

    fun addToCalendar(title: String, description: String) {
        safeStart(
            Intent(Intent.ACTION_INSERT).apply {
                data = Uri.parse("content://com.android.calendar/events")
                putExtra("title", title)
                putExtra("description", description)
            }
        )
    }

    val appointmentMsg = """
Hello $doctorName,
I want to book an appointment.

Preferred time: (today/tomorrow) - (morning/afternoon/evening)
Reason: (brief symptoms or consultation)

Sent from MediCap.
""".trimIndent()

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF)
        )
    )

    Scaffold(
        topBar = { MediCapTopBar(title = "Emergency", onBack = onBack) },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(gradient)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Premium alert hero
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFFFF7ED), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "In an emergency, call immediately",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "MediCap is informational only — not a substitute for medical care.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick actions (premium row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAction(
                    title = "Emergency",
                    subtitle = "112",
                    icon = Icons.Filled.Security,
                    bg = Color(0xFF0F172A),
                    fg = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { dial("112") }
                )
                QuickAction(
                    title = "Ambulance",
                    subtitle = "108",
                    icon = Icons.Filled.LocalHospital,
                    bg = Color(0xFFF1F5F9),
                    fg = Ink,
                    modifier = Modifier.weight(1f),
                    onClick = { dial("108") }
                )
            }

            // Doctor card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PhoneInTalk, contentDescription = null, tint = Ink)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doctorName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (doctorPhone.isBlank()) "Number not set" else "Phone: $doctorPhone",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (doctorPhone.isBlank()) Color(0xFFFFE4E6) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (doctorPhone.isBlank()) "Setup" else "Ready",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (doctorPhone.isBlank()) Color(0xFF9F1239) else Color(0xFF166534),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = { if (doctorPhone.isBlank()) dial("") else dial(doctorPhone) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null)
                        Spacer(Modifier.size(10.dp))
                        Text("Call $doctorName", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Booking card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Book appointment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )
                    Text(
                        text = "Choose the fastest option. You can also add it to calendar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )

                    OutlinedButton(
                        onClick = { if (doctorPhone.isBlank()) dial("") else dial(doctorPhone) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null)
                        Spacer(Modifier.size(10.dp))
                        Text("Book via Call", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { openWhatsApp(whatsappNumber, appointmentMsg) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = whatsappNumber.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = null)
                        Spacer(Modifier.size(10.dp))
                        Text("Book via WhatsApp", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = {
                            addToCalendar(
                                title = "Doctor Appointment - $doctorName",
                                description = appointmentMsg
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.size(10.dp))
                        Text("Add to Calendar", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    if (whatsappNumber.isBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "Tip: Add doctor WhatsApp number to enable WhatsApp booking.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        }
                    }
                }
            }

            // Fake premium history (UI only)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = Ink)
                        Text(
                            text = "Recent actions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Ink
                        )
                    }

                    HistoryRow("Called Ambulance", "108 • 2 days ago")
                    HistoryRow("Shared appointment message", "WhatsApp • 5 days ago")
                    HistoryRow("Added appointment", "Calendar • 1 week ago")
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = fg)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = fg.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

