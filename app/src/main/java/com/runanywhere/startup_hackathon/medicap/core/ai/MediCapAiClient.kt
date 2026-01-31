package com.runanywhere.startup_hackathon.medicap.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Thin adapter on top of the RunAnywhere template chat logic.
 * Implementation will be wired by delegating to the existing ChatViewModel/engine.
 */
interface MediCapAiClient {
    val isModelReady: Boolean
    fun generateSummaryStream(medicineDisplay: String): Flow<String>
}
