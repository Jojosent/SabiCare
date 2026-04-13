package com.example.sabicare_j.utils

/**
 * WHO Growth Standards for newborns (0-12 months)
 * Values based on WHO Child Growth Standards
 * https://www.who.int/tools/child-growth-standards
 */
object GrowthStandards {

    data class NormRange(
        val min: Double,
        val max: Double,
        val average: Double
    )

    // ─── HEIGHT norms by age in weeks (cm) ───────────────────────────────────
    // Gender: MALE / FEMALE
    private val heightNormsMale = mapOf(
        0 to NormRange(46.1, 53.7, 49.9),
        1 to NormRange(47.5, 55.1, 51.2),
        2 to NormRange(49.2, 56.9, 52.9),
        4 to NormRange(51.0, 59.0, 54.7),
        6 to NormRange(53.0, 61.1, 56.7),
        8 to NormRange(54.7, 62.9, 58.4),
        10 to NormRange(56.3, 64.6, 60.1),
        12 to NormRange(57.6, 66.0, 61.4),
        16 to NormRange(60.0, 68.8, 63.9),
        20 to NormRange(62.1, 71.3, 66.2),
        24 to NormRange(64.0, 73.4, 68.0),
        36 to NormRange(67.3, 77.1, 71.6),
        48 to NormRange(70.0, 80.5, 74.7)
    )

    private val heightNormsFemale = mapOf(
        0 to NormRange(45.4, 52.9, 49.1),
        1 to NormRange(46.8, 54.3, 50.5),
        2 to NormRange(48.4, 56.1, 52.0),
        4 to NormRange(50.0, 58.1, 53.7),
        6 to NormRange(51.9, 60.1, 55.6),
        8 to NormRange(53.5, 62.0, 57.3),
        10 to NormRange(55.0, 63.7, 58.9),
        12 to NormRange(56.2, 65.0, 60.0),
        16 to NormRange(58.4, 67.6, 62.0),
        20 to NormRange(60.5, 70.1, 64.0),
        24 to NormRange(62.3, 72.0, 65.7),
        36 to NormRange(65.6, 75.8, 69.2),
        48 to NormRange(68.3, 79.2, 72.8)
    )

    // ─── WEIGHT norms by age in weeks (grams) ────────────────────────────────
    private val weightNormsMale = mapOf(
        0 to NormRange(2500.0, 4300.0, 3300.0),
        1 to NormRange(2600.0, 4500.0, 3500.0),
        2 to NormRange(2900.0, 5000.0, 3900.0),
        4 to NormRange(3400.0, 5800.0, 4500.0),
        6 to NormRange(4000.0, 6600.0, 5100.0),
        8 to NormRange(4500.0, 7400.0, 5600.0),
        10 to NormRange(5000.0, 8000.0, 6200.0),
        12 to NormRange(5300.0, 8600.0, 6400.0),
        16 to NormRange(6000.0, 9700.0, 7400.0),
        20 to NormRange(6700.0, 10600.0, 8000.0),
        24 to NormRange(7100.0, 11300.0, 8600.0),
        36 to NormRange(8000.0, 12500.0, 9600.0),
        48 to NormRange(8600.0, 13600.0, 10200.0)
    )

    private val weightNormsFemale = mapOf(
        0 to NormRange(2400.0, 4200.0, 3200.0),
        1 to NormRange(2500.0, 4300.0, 3300.0),
        2 to NormRange(2800.0, 4800.0, 3600.0),
        4 to NormRange(3200.0, 5500.0, 4200.0),
        6 to NormRange(3600.0, 6200.0, 4700.0),
        8 to NormRange(4100.0, 7000.0, 5100.0),
        10 to NormRange(4500.0, 7700.0, 5700.0),
        12 to NormRange(4800.0, 8200.0, 6100.0),
        16 to NormRange(5400.0, 9300.0, 6700.0),
        20 to NormRange(6000.0, 10200.0, 7500.0),
        24 to NormRange(6400.0, 10900.0, 7900.0),
        36 to NormRange(7200.0, 12100.0, 8900.0),
        48 to NormRange(7800.0, 13100.0, 9500.0)
    )

    // ─── FEEDINGS norms by age in months (times per day) ─────────────────────
    // Same for both genders
    private val feedingsNorms = mapOf(
        0 to NormRange(8.0, 12.0, 10.0),
        1 to NormRange(7.0, 12.0, 9.0),
        2 to NormRange(6.0, 10.0, 8.0),
        3 to NormRange(5.0, 8.0, 7.0),
        4 to NormRange(5.0, 7.0, 6.0),
        5 to NormRange(4.0, 7.0, 6.0),
        6 to NormRange(4.0, 6.0, 5.0),
        9 to NormRange(3.0, 5.0, 4.0),
        12 to NormRange(3.0, 4.0, 3.5)
    )

    // ─── CALORIES norms by age in months (kcal per day) ──────────────────────
    private val caloriesNorms = mapOf(
        0 to NormRange(300.0, 500.0, 400.0),
        1 to NormRange(350.0, 550.0, 450.0),
        2 to NormRange(400.0, 600.0, 500.0),
        3 to NormRange(450.0, 650.0, 550.0),
        4 to NormRange(500.0, 700.0, 600.0),
        5 to NormRange(550.0, 750.0, 650.0),
        6 to NormRange(600.0, 800.0, 700.0),
        9 to NormRange(700.0, 950.0, 800.0),
        12 to NormRange(800.0, 1100.0, 950.0)
    )

    // ─── SLEEP norms by age in months (minutes per day) ──────────────────────
    private val sleepNorms = mapOf(
        0 to NormRange(900.0, 1020.0, 960.0),   // 15-17 hours
        1 to NormRange(840.0, 1020.0, 930.0),   // 14-17 hours
        2 to NormRange(840.0, 1020.0, 930.0),
        3 to NormRange(840.0, 990.0, 900.0),    // 14-16.5 hours
        4 to NormRange(780.0, 960.0, 870.0),    // 13-16 hours
        6 to NormRange(780.0, 930.0, 840.0),    // 13-15.5 hours
        9 to NormRange(720.0, 900.0, 810.0),    // 12-15 hours
        12 to NormRange(660.0, 840.0, 750.0)    // 11-14 hours
    )

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Get height norm for a child of given age (in days) and gender.
     */
    fun getHeightNorm(ageInDays: Int, gender: String): NormRange {
        val ageInWeeks = ageInDays / 7
        return getClosestNorm(if (gender == "MALE") heightNormsMale else heightNormsFemale, ageInWeeks)
    }

    fun getWeightNorm(ageInDays: Int, gender: String): NormRange {
        val ageInWeeks = ageInDays / 7
        return getClosestNorm(if (gender == "MALE") weightNormsMale else weightNormsFemale, ageInWeeks)
    }

    fun getFeedingsNorm(ageInDays: Int): NormRange {
        val ageInMonths = ageInDays / 30
        return getClosestNorm(feedingsNorms, ageInMonths)
    }

    fun getCaloriesNorm(ageInDays: Int): NormRange {
        val ageInMonths = ageInDays / 30
        return getClosestNorm(caloriesNorms, ageInMonths)
    }

    fun getSleepNorm(ageInDays: Int): NormRange {
        val ageInMonths = ageInDays / 30
        return getClosestNorm(sleepNorms, ageInMonths)
    }

    /**
     * Find the closest age key in the norm map and return its range.
     */
    private fun getClosestNorm(map: Map<Int, NormRange>, age: Int): NormRange {
        val sortedKeys = map.keys.sorted()
        val closestKey = sortedKeys.minByOrNull { kotlin.math.abs(it - age) } ?: sortedKeys.first()
        return map[closestKey]!!
    }

    /**
     * Calculate compliance percentage (0-100+).
     * 100 = exactly at average, 0 = at or below min, 200+ = way above max
     */
    fun getCompliancePercent(value: Double, norm: NormRange): Int {
        return when {
            value <= norm.min -> (value / norm.min * 50).toInt()
            value <= norm.average -> (50 + (value - norm.min) / (norm.average - norm.min) * 50).toInt()
            value <= norm.max -> (100 - (value - norm.average) / (norm.max - norm.average) * 20).toInt()
            else -> 100
        }.coerceIn(0, 100)
    }

    enum class ComplianceStatus { CRITICAL, BELOW, NORMAL, ABOVE }

    fun getStatus(value: Double, norm: NormRange): ComplianceStatus {
        return when {
            value < norm.min * 0.9 -> ComplianceStatus.CRITICAL
            value < norm.min -> ComplianceStatus.BELOW
            value > norm.max * 1.1 -> ComplianceStatus.ABOVE
            else -> ComplianceStatus.NORMAL
        }
    }
}