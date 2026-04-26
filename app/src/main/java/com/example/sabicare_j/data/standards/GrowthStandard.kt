package com.example.sabicare_j.data.standards

data class GrowthStandard(
    val age: String,
    val age_months: Double,
    val weight_b: Double,  // boys
    val height_b: Double,
    val weight_g: Double,  // girls
    val height_g: Double,
    val feedings: String,
    val sleep: String,
    val kcal_week: Int
)

data class GrowthStandardsWrapper(
    val growth_standards: List<GrowthStandard>
)