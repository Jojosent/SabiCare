package com.example.sabicare_j.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREF = "sabicare_prefs_simple"
    private const val KEY_LANG = "language"
    const val DEFAULT_LANG = "kk"

    /**
     * Apply a locale to a Context.
     * Call this in Activity.attachBaseContext() and Application.attachBaseContext().
     */
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /** Returns locale code from stored pref. Default is [DEFAULT_LANG]. */
    fun getSavedLocale(context: Context): String {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    fun saveLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, languageCode).apply()
    }
}
