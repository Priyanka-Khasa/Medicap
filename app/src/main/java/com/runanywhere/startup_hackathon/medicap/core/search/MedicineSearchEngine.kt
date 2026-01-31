package com.runanywhere.startup_hackathon.medicap.core.search

import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import kotlin.math.sqrt

object MedicineSearchEngine {

    fun rerank(
        queryEmbedding: FloatArray,
        medicines: List<MedicineEntity>,
        medicineEmbeddings: Map<Long, FloatArray>
    ): List<MedicineEntity> {
        return medicines.sortedByDescending { med ->
            val emb = medicineEmbeddings[med.id] ?: return@sortedByDescending 0f
            cosineSimilarity(queryEmbedding, emb)
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val n = minOf(a.size, b.size)
        var dot = 0f
        var magA = 0f
        var magB = 0f

        for (i in 0 until n) {
            val ai = a[i]
            val bi = b[i]
            dot += ai * bi
            magA += ai * ai
            magB += bi * bi
        }

        return dot / (sqrt(magA) * sqrt(magB) + 1e-6f)
    }
}
