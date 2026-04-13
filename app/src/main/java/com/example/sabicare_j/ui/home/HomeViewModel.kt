package com.example.sabicare_j.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.data.repository.MeasurementRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ReminderItem(
    val type: MeasurementType,
    val isDue: Boolean,
    val daysUntilNext: Int,
    val lastValueText: String
)

data class ArticleItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val category: String
)

data class MedicationItem(
    val emoji: String,
    val name: String,
    val description: String,
    val ageRange: String
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val measurementRepo: MeasurementRepository =
        (application as SabiCareApplication).measurementRepository

    private val _reminders = MutableLiveData<List<ReminderItem>>()
    val reminders: LiveData<List<ReminderItem>> = _reminders

    private val _articles = MutableLiveData<List<ArticleItem>>()
    val articles: LiveData<List<ArticleItem>> = _articles

    private val _medications = MutableLiveData<List<MedicationItem>>()
    val medications: LiveData<List<MedicationItem>> = _medications

    init {
        loadArticles()
        loadMedications()
    }

    fun loadRemindersForChild(childId: Long) {
        viewModelScope.launch {
            val items = MeasurementType.values().map { type ->
                val latest = measurementRepo.getLatestByType(childId, type)
                val isDue = measurementRepo.isDue(childId, type)
                val daysUntil = measurementRepo.daysUntilNext(childId, type)

                val lastValueText = if (latest != null) {
                    "${formatValue(latest.value)} ${type.unit}"
                } else {
                    "Деректер жоқ"
                }

                ReminderItem(
                    type = type,
                    isDue = isDue,
                    daysUntilNext = daysUntil,
                    lastValueText = lastValueText
                )
            }
            // Sort: due first, then by days until next
            val sorted = items.sortedWith(
                compareByDescending<ReminderItem> { it.isDue }
                    .thenBy { it.daysUntilNext }
            )
            _reminders.postValue(sorted)
        }
    }

    private fun loadArticles() {
        val articles = listOf(
            ArticleItem(
                emoji = "🤱",
                title = "Емізудің дұрыс техникасы",
                subtitle = "Жаңа туылған нәрестені дұрыс емізу жолдары",
                category = "Тамақтану"
            ),
            ArticleItem(
                emoji = "😴",
                title = "Нәрестенің ұйқы режимі",
                subtitle = "0-3 айлық баланың ұйқы кестесі қалай болу керек",
                category = "Ұйқы"
            ),
            ArticleItem(
                emoji = "🛁",
                title = "Алғашқы шомылдыру",
                subtitle = "Нәрестені қауіпсіз шомылдыру бойынша нұсқаулар",
                category = "Күтім"
            ),
            ArticleItem(
                emoji = "📈",
                title = "Дамудың негізгі кезеңдері",
                subtitle = "Айына 1-12 аралығындағы маңызды даму белестері",
                category = "Даму"
            ),
            ArticleItem(
                emoji = "🌡️",
                title = "Температураны қашан өлшеу керек",
                subtitle = "Қашан дәрігерге хабарласу керектігін білу",
                category = "Денсаулық"
            ),
            ArticleItem(
                emoji = "💪",
                title = "Нәрестелік гимнастика",
                subtitle = "1 айдан бастап қарапайым жаттығулар",
                category = "Физикалық даму"
            )
        )
        _articles.postValue(articles)
    }

    private fun loadMedications() {
        val meds = listOf(
            MedicationItem(
                emoji = "☀️",
                name = "Д витамині",
                description = "Сүйек жүйесін нығайтады, рахитті алдын алады",
                ageRange = "0+ ай"
            ),
            MedicationItem(
                emoji = "🩸",
                name = "Темір препараттары",
                description = "Анемияның алдын алу үшін қажет",
                ageRange = "4+ ай"
            ),
            MedicationItem(
                emoji = "🐟",
                name = "Омега-3",
                description = "Мидың дамуы мен иммунитет үшін",
                ageRange = "6+ ай"
            ),
            MedicationItem(
                emoji = "🦷",
                name = "Фтор",
                description = "Тістің дамуы үшін, педиатрмен кеңесіп қолдану",
                ageRange = "6+ ай"
            )
        )
        _medications.postValue(meds)
    }

    private fun formatValue(value: Double): String {
        return if (value == value.toLong().toDouble())
            value.toLong().toString()
        else "%.1f".format(value)
    }

    fun getAgeString(birthDateMillis: Long): String {
        val diffMs = System.currentTimeMillis() - birthDateMillis
        val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        return when {
            days < 7 -> "$days күн"
            days < 30 -> "${days / 7} апта"
            days < 365 -> "${days / 30} ай"
            else -> "${days / 365} жас ${(days % 365) / 30} ай"
        }
    }
}