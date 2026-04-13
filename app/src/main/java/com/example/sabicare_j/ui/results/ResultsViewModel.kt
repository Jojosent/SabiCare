package com.example.sabicare_j.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.local.entities.MeasurementEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.utils.GrowthStandards
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ResultCardState(
    val type: MeasurementType,
    val latestValue: Double?,
    val norm: GrowthStandards.NormRange?,
    val compliancePercent: Int,
    val status: GrowthStandards.ComplianceStatus,
    val history: List<MeasurementEntity>
)

class ResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val measurementRepo =
        (application as SabiCareApplication).measurementRepository

    private val _resultCards = MutableLiveData<List<ResultCardState>>()
    val resultCards: LiveData<List<ResultCardState>> = _resultCards

    private val _currentChild = MutableLiveData<ChildEntity?>()
    val currentChild: LiveData<ChildEntity?> = _currentChild

    fun loadForChild(child: ChildEntity) {
        _currentChild.value = child
        viewModelScope.launch {
            val ageInDays = getAgeInDays(child.birthDate)
            val cards = MeasurementType.values().map { type ->
                buildCard(child, type, ageInDays)
            }
            _resultCards.postValue(cards)
        }
    }

    private suspend fun buildCard(
        child: ChildEntity,
        type: MeasurementType,
        ageInDays: Int
    ): ResultCardState {
        val history = measurementRepo.getMeasurementsByType(child.id, type)
        val latest = history.lastOrNull()

        val norm = when (type) {
            MeasurementType.HEIGHT -> GrowthStandards.getHeightNorm(ageInDays, child.gender)
            MeasurementType.WEIGHT -> GrowthStandards.getWeightNorm(ageInDays, child.gender)
            MeasurementType.FEEDINGS_COUNT -> GrowthStandards.getFeedingsNorm(ageInDays)
            MeasurementType.CALORIES -> GrowthStandards.getCaloriesNorm(ageInDays)
            MeasurementType.SLEEP_DURATION -> GrowthStandards.getSleepNorm(ageInDays)
        }

        val compliance = if (latest != null)
            GrowthStandards.getCompliancePercent(latest.value, norm) else 0

        val status = if (latest != null)
            GrowthStandards.getStatus(latest.value, norm)
        else GrowthStandards.ComplianceStatus.BELOW

        return ResultCardState(
            type = type,
            latestValue = latest?.value,
            norm = norm,
            compliancePercent = compliance,
            status = status,
            history = history
        )
    }

    private fun getAgeInDays(birthDateMillis: Long): Int {
        val diff = System.currentTimeMillis() - birthDateMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
    }
}