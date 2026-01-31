package com.runanywhere.startup_hackathon.medicap.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon.medicap.ui.components.MediCapTopBar
import com.runanywhere.startup_hackathon.medicap.ui.delivery.CartItem
import com.runanywhere.startup_hackathon.medicap.ui.delivery.DeliveryViewModel

private val ScreenBg = Color(0xFFF8FAFC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryHubScreen(
    onBack: () -> Unit,
    vm: DeliveryViewModel = viewModel()
) {
    val context = LocalContext.current
    val cart by vm.cart.collectAsState()
    var notes by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color.White)
    )

    fun safeShare(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MediCap Delivery Request")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Share order via"))
        }
    }

    val totalQty = cart.sumOf { it.qty }
    val fakeEtaMin = 25 // premium look (fake but stable)
    val fakePharmacyCount = 18 // premium look (fake but stable)

    Scaffold(
        topBar = { MediCapTopBar(title = "Delivery Hub", onBack = onBack) },
        containerColor = ScreenBg,
        bottomBar = {
            BottomAppBar {
                TextButton(
                    onClick = { vm.clear() },
                    enabled = cart.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Clear")
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { safeShare(vm.buildOrderText(notes)) },
                    enabled = cart.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Share Order")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ✅ Premium hero card
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
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Request medicine delivery",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Share your cart to any pharmacy in seconds",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        StatusPill(
                            text = if (cart.isEmpty()) "Empty" else "Ready",
                            good = cart.isNotEmpty()
                        )
                    }

                    Divider()

                    // ✅ premium stats (fake numbers allowed)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Items",
                            value = "${cart.size}",
                            icon = Icons.Filled.ShoppingCart,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total Qty",
                            value = "$totalQty",
                            icon = Icons.Filled.Inventory2,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "ETA",
                            value = "${fakeEtaMin}m",
                            icon = Icons.Filled.Verified,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        "Nearby pharmacies: $fakePharmacyCount (approx.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            // ✅ Notes card (premium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Notes for pharmacy",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Optional") },
                        placeholder = { Text("Generic allowed / urgent / delivery address…") },
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Text(
                        "Tip: Mention preferred brand, dosage, and delivery time window.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            if (cart.isEmpty()) {
                EmptyCartCard()
                Spacer(Modifier.height(60.dp))
                return@Column
            }

            // ✅ Cart header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cart items",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${cart.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = cart, key = { it.medicineId }) { item ->
                    CartItemCardPro(
                        item = item,
                        onInc = { vm.inc(item.medicineId) },
                        onDec = { vm.dec(item.medicineId) }
                    )
                }
                item { Spacer(Modifier.height(90.dp)) } // space for bottom bar
            }
        }
    }
}

@Composable
private fun EmptyCartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Ink, modifier = Modifier.size(30.dp))
            }

            Text(
                "Your cart is empty",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Ink
            )
            Text(
                "Open any medicine details and tap “Add to Delivery Hub”.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}

@Composable
private fun CartItemCardPro(
    item: CartItem,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Ink)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Code: ${item.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Qty controls (premium pill)
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = onDec, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = item.qty.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Ink
                    )

                    IconButton(onClick = onInc, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Ink)
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    title,
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
private fun StatusPill(text: String, good: Boolean) {
    val bg = if (good) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
    val fg = if (good) Color(0xFF166534) else Color(0xFF9F1239)

    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
