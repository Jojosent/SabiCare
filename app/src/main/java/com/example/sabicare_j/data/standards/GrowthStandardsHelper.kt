package com.example.sabicare_j.data.standards

import android.content.Context
import org.json.JSONObject

object GrowthStandardsHelper {

    private var standards: List<GrowthStandard>? = null

    fun load(context: Context): List<GrowthStandard> {
        if (standards != null) return standards!!
        val json = context.assets.open("standart.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("growth_standards")
        val list = mutableListOf<GrowthStandard>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(GrowthStandard(
                age = o.getString("age"),
                age_months = o.getDouble("age_months"),
                weight_b = o.getDouble("weight_b"),
                height_b = o.getDouble("height_b"),
                weight_g = o.getDouble("weight_g"),
                height_g = o.getDouble("height_g"),
                feedings = o.getString("feedings"),
                sleep = o.getString("sleep"),
                kcal_week = o.getInt("kcal_week")
            ))
        }
        standards = list
        return list
    }

    /**
     * Балаға жас бойынша ең жақын стандартты табу
     * @param ageMonths - айлардағы жас
     * @param isMale - ұл ба
     */
    fun getStandardForAge(
        context: Context,
        ageMonths: Double,
        isMale: Boolean
    ): GrowthStandard? {
        val list = load(context)
        // Ең жақын жасты табу (interpolation емес, nearest)
        return list.minByOrNull { Math.abs(it.age_months - ageMonths) }
    }

    /**
     * Салмақ нормасын алу (кг)
     */
    fun getWeightNorm(context: Context, ageMonths: Double, isMale: Boolean): Double? {
        val std = getStandardForAge(context, ageMonths, isMale) ?: return null
        return if (isMale) std.weight_b else std.weight_g
    }

    /**
     * Бой нормасын алу (см)
     */
    fun getHeightNorm(context: Context, ageMonths: Double, isMale: Boolean): Double? {
        val std = getStandardForAge(context, ageMonths, isMale) ?: return null
        return if (isMale) std.height_b else std.height_g
    }

    /**
     * Тамақтандыру саны нормасы
     */
    fun getFeedingsNorm(context: Context, ageMonths: Double): String? {
        return getStandardForAge(context, ageMonths, true)?.feedings
    }

    /**
     * Ұйқы нормасы (сағатпен)
     */
    fun getSleepNorm(context: Context, ageMonths: Double): String? {
        return getStandardForAge(context, ageMonths, true)?.sleep
    }

    /**
     * Калория нормасы (аптасына)
     */
    fun getKcalNorm(context: Context, ageMonths: Double): Int? {
        return getStandardForAge(context, ageMonths, true)?.kcal_week
    }

    /**
     * Мәндің нормадан ауытқуын тексеру
     * Қайтарады: "NORMAL", "BELOW", "ABOVE"
     */
    fun checkWeightStatus(context: Context, ageMonths: Double, isMale: Boolean, valueKg: Double): String {
        val norm = getWeightNorm(context, ageMonths, isMale) ?: return "NORMAL"
        val ratio = valueKg / norm
        return when {
            ratio < 0.85 -> "BELOW"
            ratio > 1.20 -> "ABOVE"
            else -> "NORMAL"
        }
    }

    fun checkHeightStatus(context: Context, ageMonths: Double, isMale: Boolean, valueCm: Double): String {
        val norm = getHeightNorm(context, ageMonths, isMale) ?: return "NORMAL"
        val diff = valueCm - norm
        return when {
            diff < -3.0 -> "BELOW"
            diff > 3.0 -> "ABOVE"
            else -> "NORMAL"
        }
    }
}