package com.runanywhere.startup_hackathon.medicap.data.seed

import android.content.Context
import com.google.gson.stream.JsonReader
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object NdhmSeeder {

    private const val ASSET_FILE = "ndhm_medicine_codes.json"
    private const val CHUNK_SIZE = 1000

    suspend fun seedIfEmpty(context: Context, db: AppDatabase) {
        val existing = withContext(Dispatchers.IO) { db.medicineDao().count() }
        if (existing > 0) return

        withContext(Dispatchers.IO) {
            context.assets.open(ASSET_FILE).use { input ->
                JsonReader(InputStreamReader(input)).use { reader ->
                    streamParseAndInsert(reader, db)
                }
            }
        }
    }

    private suspend fun streamParseAndInsert(reader: JsonReader, db: AppDatabase) {
        // We only care about: compose -> include[] -> concept[] -> {code, display}
        val chunk = ArrayList<MedicineEntity>(CHUNK_SIZE)

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "compose" -> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "include" -> {
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    parseInclude(reader, chunk, db)
                                }
                                reader.endArray()
                            }
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        // flush remaining
        if (chunk.isNotEmpty()) {
            db.medicineDao().insertAll(chunk)
            chunk.clear()
        }
    }

    private suspend fun parseInclude(reader: JsonReader, chunk: ArrayList<MedicineEntity>, db: AppDatabase) {
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "concept" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val med = parseConcept(reader)
                        if (med != null) {
                            chunk.add(med)
                            if (chunk.size >= CHUNK_SIZE) {
                                db.medicineDao().insertAll(chunk)
                                chunk.clear()
                            }
                        }
                    }
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
    }

    private fun parseConcept(reader: JsonReader): MedicineEntity? {
        var code: String? = null
        var display: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "code" -> code = safeNextString(reader)
                "display" -> display = safeNextString(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        val c = code?.trim()
        val d = display?.trim()
        if (c.isNullOrBlank() || d.isNullOrBlank()) return null

        return MedicineEntity(code = c, display = d)
    }

    private fun safeNextString(reader: JsonReader): String? {
        return try {
            if (reader.peek().name == "NULL") {
                reader.nextNull()
                null
            } else reader.nextString()
        } catch (_: Exception) {
            reader.skipValue()
            null
        }
    }
}
