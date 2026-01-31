package com.runanywhere.startup_hackathon.medicap.core.ai

import com.runanywhere.sdk.public.RunAnywhere
import kotlinx.coroutines.flow.Flow

object MediCapAi {

    fun buildPrompt(medicineDisplay: String): String {
        return """
You are MediCap, an offline medicine information assistant.
Be professional, concise, and safe. Do NOT diagnose. If uncertain, say so.
Return bullet points. Avoid emojis.

Medicine name/formulation: "$medicineDisplay"

Give:
1) What it is used for (common uses)
2) How it is commonly taken (general guidance, no exact dose for children/pregnancy)
3) Warnings / who should be careful
4) Common side effects
5) Seek urgent help if (red flags)
End with: "This is informational, not medical advice."
""".trimIndent()
    }

    fun generateSummaryStream(medicineDisplay: String): Flow<String> {
        val prompt = buildPrompt(medicineDisplay)
        return RunAnywhere.generateStream(prompt)
    }
}
