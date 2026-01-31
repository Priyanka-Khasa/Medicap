package com.runanywhere.startup_hackathon.medicap.ui.delivery

import androidx.lifecycle.ViewModel
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val medicineId: Long,
    val name: String,
    val code: String,
    val qty: Int
)

class DeliveryViewModel : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    fun addToCart(med: MedicineEntity) {
        _cart.update { old ->
            val existing = old.firstOrNull { it.medicineId == med.id }
            if (existing == null) {
                old + CartItem(
                    medicineId = med.id,
                    name = med.display,
                    code = med.code,
                    qty = 1
                )
            } else {
                old.map { if (it.medicineId == med.id) it.copy(qty = it.qty + 1) else it }
            }
        }
    }

    fun inc(id: Long) {
        _cart.update { list -> list.map { if (it.medicineId == id) it.copy(qty = it.qty + 1) else it } }
    }

    fun dec(id: Long) {
        _cart.update { list ->
            list.mapNotNull {
                if (it.medicineId != id) it
                else {
                    val q = it.qty - 1
                    if (q <= 0) null else it.copy(qty = q)
                }
            }
        }
    }

    fun clear() {
        _cart.value = emptyList()
    }

    fun buildOrderText(userNotes: String): String {
        val items = _cart.value
        val lines = items.map { "• ${it.name} (code: ${it.code}) × ${it.qty}" }
        val notes = userNotes.trim().ifBlank { "—" }

        return """
MediCap Delivery Request

Items:
${if (lines.isEmpty()) "—" else lines.joinToString("\n")}

Notes:
$notes

Please confirm availability, price, and delivery time.
(Share prescription if required.)
        """.trim()
    }
}
