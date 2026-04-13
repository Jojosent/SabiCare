package com.example.sabicare_j.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create DataStore instance once per app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sabicare_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")       // "kz", "ru", "en"
        val KEY_THEME = stringPreferencesKey("theme")             // "light", "dark", "system"
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    // ─── Language ─────────────────────────────────────────────────────────────

    val languageFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LANGUAGE] ?: "ru" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    // ─── Theme ────────────────────────────────────────────────────────────────

    val themeFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_THEME] ?: "system" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    val notificationsFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATIONS] ?: true }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    // ─── First launch ─────────────────────────────────────────────────────────

    val firstLaunchFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { it[KEY_FIRST_LAUNCH] = isFirst }
    }
}