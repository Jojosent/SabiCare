package com.example.sabicare_j.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Apply a locale to a Context.
     * Call this in Activity.attachBaseContext() and Application.
     */
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Returns locale code from stored pref.
     * Default is "ru".
     */
    fun getSavedLocale(context: Context): String {
        val prefs = context.getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
        return prefs.getString("language", "ru") ?: "ru"
    }

    fun saveLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
    }
}