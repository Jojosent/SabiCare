package com.example.sabicare_j.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Holds the current theme preference as observable state so Compose
 * recomposes when it changes (no activity restart required).
 *
 * Valid values: "light" | "dark" | "system"
 */
object ThemeManager {

    private const val PREF = "sabicare_prefs_simple"
    private const val KEY = "theme"
    const val DEFAULT = "system"

    val state: MutableState<String> = mutableStateOf(DEFAULT)
    private var initialised = false

    fun init(context: Context) {
        if (initialised) return
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val theme = prefs.getString(KEY, DEFAULT) ?: DEFAULT
        state.value = theme
        applyDelegate(theme)
        initialised = true
    }

    fun setTheme(theme: String) {
        state.value = theme
        applyDelegate(theme)
    }

    private fun applyDelegate(theme: String) {
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
            else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
