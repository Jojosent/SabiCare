package com.example.sabicare_j.ui.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.MeasurementEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.data.repository.MeasurementRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MeasurementCardState(
    val type: MeasurementType,
    val latestValue: Double?,
    val latestDateMillis: Long?,
    val isDue: Boolean,
    val daysUntilNext: Int  // negative = overdue
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val measurementRepo: MeasurementRepository =
        (application as SabiCareApplication).measurementRepository

    // Current child ID (set from TrackerFragment when activeChild changes)
    private val _currentChildId = MutableLiveData<Long>()

    // Cards state for all 5 measurement types
    private val _cards = MutableLiveData<List<MeasurementCardState>>()
    val cards: LiveData<List<MeasurementCardState>> = _cards

    fun loadForChild(childId: Long) {
        _currentChildId.value = childId
        refreshCards(childId)
    }

    fun refreshCards(childId: Long) {
        viewModelScope.launch {
            val states = MeasurementType.values().map { type ->
                val latest = measurementRepo.getLatestByType(childId, type)
                val isDue = measurementRepo.isDue(childId, type)
                val daysUntil = measurementRepo.daysUntilNext(childId, type)

                MeasurementCardState(
                    type = type,
                    latestValue = latest?.value,
                    latestDateMillis = latest?.recordedAt,
                    isDue = isDue,
                    daysUntilNext = daysUntil
                )
            }
            _cards.postValue(states)
        }
    }

    fun addMeasurement(
        childId: Long,
        type: MeasurementType,
        value: Double,
        note: String?,
        recordedAt: Long
    ) {
        viewModelScope.launch {
            val entity = MeasurementEntity(
                childId = childId,
                type = type.name,
                value = value,
                note = note,
                recordedAt = recordedAt
            )
            measurementRepo.addMeasurement(entity)
            refreshCards(childId)
        }
    }

    fun getHistoryForType(childId: Long, type: MeasurementType): LiveData<List<MeasurementEntity>> {
        return measurementRepo.getMeasurementsByTypeLive(childId, type)
    }
}