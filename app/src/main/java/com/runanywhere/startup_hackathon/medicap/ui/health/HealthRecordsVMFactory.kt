package com.runanywhere.startup_hackathon.medicap.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.runanywhere.startup_hackathon.medicap.data.dao.HealthDao

class HealthRecordsVMFactory(private val dao: HealthDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthRecordsViewModel::class.java)) {
            return HealthRecordsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
