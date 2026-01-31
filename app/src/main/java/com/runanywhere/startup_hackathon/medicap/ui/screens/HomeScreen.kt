package com.runanywhere.startup_hackathon.medicap.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ✅ Premium palette (safe, independent)
private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val CardWhite = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onExpiryVault: () -> Unit,
    onDeliveryHub: () -> Unit,
    onEmergency: () -> Unit,
    onHealthRecords: () -> Unit,
    onModels: (() -> Unit)? = null,
    onOpenMedicineDetails: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).medicineDao() }
    var topMeds by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        topMeds = runCatching {
            withContext(Dispatchers.IO) { dao.getTop(limit = 12) }
        }.getOrElse { emptyList() }
    }

    Scaffold(
        topBar = { MediCapTopBar(title = "MediCap", onBack = null) },
        containerColor = ScreenBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ScreenBg),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                PremiumHeroSection(
                    onScan = onScan,
                    onSearch = onSearch,
                    onDeliveryHub = onDeliveryHub,
                    onEmergency = onEmergency
                )
            }

            item { PremiumSearchCard(onClick = onSearch) }

            item {
                PremiumQuickActions(
                    onScan = onScan,
                    onSearch = onSearch,
                    onExpiryVault = onExpiryVault,
                    onDeliveryHub = onDeliveryHub,
                    onEmergency = onEmergency,
                    onHealthRecords = onHealthRecords,
                    onModels = onModels
                )
            }

            item {
                if (topMeds.isNotEmpty()) {
                    MedicinesSection(
                        medicines = topMeds,
                        onOpen = { id -> onOpenMedicineDetails?.invoke(id) },
                        onViewAll = onSearch
                    )
                } else {
                    EmptySection(onSearch = onSearch)
                }
            }

            // ✅ Make home less lengthy: fewer reviews + compact about
            item { ReviewsSectionCompact() }
            item { AboutSectionCompact() }

            item { DisclaimerSection() }
            item { Spacer(modifier = Modifier.height(22.dp)) }
        }
    }
}

/* ----------------------------- HERO ----------------------------- */

@Composable
private fun PremiumHeroSection(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onDeliveryHub: () -> Unit,
    onEmergency: () -> Unit
) {
    val context = LocalContext.current
    val heroId = remember {
        context.resources.getIdentifier("img_hero_medicap", "drawable", context.packageName)
    }

    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEEF2FF),
            Color(0xFFE6FFFB),
            Color(0xFFFFF1F2)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Your Medicine Companion",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Scan, search, expiry vault, records & emergency — offline-first.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF334155)),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // ✅ FIX: chips no longer break letter-by-letter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PremiumChip(text = "Offline-first")
                            PremiumChip(text = "Fast scan")
                            PremiumChip(text = "Secure vault")
                        }
                    }

                    if (heroId != 0) {
                        Image(
                            painter = painterResource(id = heroId),
                            contentDescription = "MediCap",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Medication,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scan now", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onSearch,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Search", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onEmergency) {
                        Icon(Icons.Filled.Emergency, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Emergency", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    TextButton(onClick = onDeliveryHub) {
                        Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delivery Hub", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.78f),
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF334155),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ----------------------------- SEARCH CARD ----------------------------- */

@Composable
private fun PremiumSearchCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF6366F1))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Search medicines",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Name • Code • Category • Company",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Muted)
        }
    }
}

/* ----------------------------- QUICK ACTIONS (FIXED VISIBILITY) ----------------------------- */

@Composable
private fun PremiumQuickActions(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onExpiryVault: () -> Unit,
    onDeliveryHub: () -> Unit,
    onEmergency: () -> Unit,
    onHealthRecords: () -> Unit,
    onModels: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap a feature to start",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AssistChip(
                onClick = onScan,
                label = { Text("Scan", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) }
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumActionItem(
                    title = "Scan",
                    subtitle = "Camera scan",
                    icon = Icons.Filled.CameraAlt,
                    accent = Color(0xFF10B981),
                    onClick = onScan,
                    modifier = Modifier.weight(1f)
                )
                PremiumActionItem(
                    title = "Search",
                    subtitle = "Offline DB",
                    icon = Icons.Filled.Search,
                    accent = Color(0xFF6366F1),
                    onClick = onSearch,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumActionItem(
                    title = "Expiry Vault",
                    subtitle = "Reminders",
                    icon = Icons.Filled.Inventory2,
                    accent = Color(0xFFF59E0B),
                    onClick = onExpiryVault,
                    modifier = Modifier.weight(1f)
                )
                PremiumActionItem(
                    title = "Delivery Hub",
                    subtitle = "Order list",
                    icon = Icons.Filled.LocalShipping,
                    accent = Color(0xFF22C55E),
                    onClick = onDeliveryHub,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumActionItem(
                    title = "Health Records",
                    subtitle = "Reports",
                    icon = Icons.Filled.Folder,
                    accent = Color(0xFF0EA5E9),
                    onClick = onHealthRecords,
                    modifier = Modifier.weight(1f)
                )
                PremiumActionItem(
                    title = "Emergency",
                    subtitle = "Quick help",
                    icon = Icons.Filled.Emergency,
                    accent = Color(0xFFEF4444),
                    onClick = onEmergency,
                    modifier = Modifier.weight(1f)
                )
            }

            if (onModels != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumActionItem(
                        title = "AI Models",
                        subtitle = "On-device",
                        icon = Icons.Filled.SmartToy,
                        accent = Color(0xFFEC4899),
                        onClick = onModels,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumActionItem(
                        title = "History",
                        subtitle = "Recent",
                        icon = Icons.Filled.History,
                        accent = Color(0xFF64748B),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(116.dp) // ✅ slightly taller -> text never gets forced into weird wraps
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            LinearProgressIndicator(
                progress = 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent.copy(alpha = 0.65f),
                trackColor = Color(0xFFF1F5F9)
            )
        }
    }
}

/* ----------------------------- MEDICINES ----------------------------- */

@Composable
private fun MedicinesSection(
    medicines: List<MedicineEntity>,
    onOpen: (Long) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Top medicines",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Quick access from your offline database",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(onClick = onViewAll) {
                Text("View all", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(medicines.take(8)) { medicine ->
                MedicineItem(medicine = medicine, onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun MedicineItem(
    medicine: MedicineEntity,
    onOpen: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onOpen(medicine.id) },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Medication, contentDescription = null, tint = Color(0xFF334155))
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.display,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = medicine.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Divider(thickness = 1.dp, color = Color(0xFFF1F5F9))

            Text(
                text = "Tap to view details",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6366F1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptySection(onSearch: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Medication,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = "No medicines added yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Search offline database or scan a medicine pack.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onSearch,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open search", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/* ----------------------------- COMPACT REVIEWS ----------------------------- */

@Composable
private fun ReviewsSectionCompact() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User reviews",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF22C55E))
        }

        Spacer(Modifier.height(10.dp))

        // ✅ fewer cards -> less lengthy
        val reviews = listOf(
            Triple("Meera", "Offline search is super helpful. Clean UI, feels premium.", 5),
            Triple("Kabir", "Expiry vault + emergency features are very practical.", 4)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews) { item ->
                ReviewCard(name = item.first, text = item.second, stars = item.third)
            }
        }
    }
}

@Composable
private fun ReviewCard(name: String, text: String, stars: Int) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { idx ->
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (idx < stars) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF22C55E))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF334155),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ----------------------------- ABOUT (FIXED PILL WRAP) ----------------------------- */

@Composable
private fun AboutSectionCompact() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "About MediCap",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // ✅ shorter text -> less lengthy
            Text(
                text = "Scan or search medicines, track expiry, and store records — built to work reliably even in low network.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF334155)
            )

            Divider(color = Color(0xFFF1F5F9))

            // ✅ FIX: equal width cards so text never becomes letter-by-letter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoTile(title = "Privacy", value = "On-device", modifier = Modifier.weight(1f))
                InfoTile(title = "Speed", value = "Fast UI", modifier = Modifier.weight(1f))
                InfoTile(title = "Trust", value = "Disclaimer", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ----------------------------- DISCLAIMER ----------------------------- */

@Composable
private fun DisclaimerSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.HealthAndSafety,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(22.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Medical disclaimer",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF92400E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Informational only. Consult a licensed professional for medical advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
