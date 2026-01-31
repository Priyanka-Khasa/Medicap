package com.runanywhere.startup_hackathon.medicap.core.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AiUiState(
    val loading: Boolean = false,
    val output: String = "",
    val error: String? = null,
    val modelReady: Boolean = true
)

class MediCapAiViewModel(
    private val client: MediCapAiClient
) : ViewModel() {

    private val _state = MutableStateFlow(AiUiState(modelReady = client.isModelReady))
    val state: StateFlow<AiUiState> = _state

    fun generate(medicineDisplay: String) {
        if (!client.isModelReady) {
            _state.value = AiUiState(modelReady = false, error = "Model not loaded. Open Models and load a model.")
            return
        }

        _state.value = _state.value.copy(loading = true, output = "", error = null, modelReady = true)

        viewModelScope.launch {
            runCatching {
                client.generateSummaryStream(medicineDisplay).collect { chunk ->
                    _state.value = _state.value.copy(output = _state.value.output + chunk)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Something went wrong")
            }
            _state.value = _state.value.copy(loading = false)
        }
    }
}
