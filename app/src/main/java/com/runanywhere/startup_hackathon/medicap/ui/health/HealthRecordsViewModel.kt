package com.runanywhere.startup_hackathon.medicap.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon.medicap.data.dao.HealthDao
import com.runanywhere.startup_hackathon.medicap.data.model.HealthProfileEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicalRecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthRecordsViewModel(
    private val dao: HealthDao
) : ViewModel() {

    val profile: StateFlow<HealthProfileEntity> =
        dao.observeProfile()
            .map { it ?: HealthProfileEntity() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HealthProfileEntity()
            )

    val records: StateFlow<List<MedicalRecordEntity>> =
        dao.observeRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun saveProfile(p: HealthProfileEntity) {
        viewModelScope.launch {
            dao.upsertProfile(p.copy(id = 1))
        }
    }

    fun addRecord(r: MedicalRecordEntity) {
        viewModelScope.launch {
            dao.insertRecord(r)
        }
    }

    fun deleteRecord(r: MedicalRecordEntity) {
        viewModelScope.launch {
            dao.deleteRecord(r)
        }
    }

    fun buildEmergencySummary(): String {
        val p = profile.value

        fun clean(value: String?): String =
            value?.trim().takeUnless { it.isNullOrBlank() } ?: "—"

        return """
MediCap Emergency Summary

Name: ${p.name.trim().ifBlank { "—" }}
Age: ${if (p.age <= 0) "—" else p.age}
Gender: ${p.gender.trim().ifBlank { "—" }}
Blood Group: ${p.bloodGroup.trim().ifBlank { "—" }}

Allergies: ${clean(p.allergies)}
Conditions: ${clean(p.conditions)}
Emergency Contact: ${clean(p.emergencyContact)}

Shared from MediCap (offline health records)
        """.trim()
    }
}
