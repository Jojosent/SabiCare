package com.example.sabicare_j

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.sabicare_j.data.local.AppDatabase
import com.example.sabicare_j.data.repository.ChildRepository
import com.example.sabicare_j.data.repository.MeasurementRepository
import com.example.sabicare_j.utils.LocaleHelper

class SabiCareApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val childRepository: ChildRepository by lazy { ChildRepository(database.childDao()) }
    val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepository(database.measurementDao())
    }

    override fun onCreate() {
        super.onCreate()
        applyStoredTheme()
    }

    override fun attachBaseContext(base: Context) {
        val lang = LocaleHelper.getSavedLocale(base) ?: "kk"
        super.attachBaseContext(LocaleHelper.applyLocale(base, lang))
    }

    private fun applyStoredTheme() {
        val prefs = getSharedPreferences("sabicare_prefs_simple", MODE_PRIVATE)
        val theme = prefs.getString("theme", "system") ?: "system"
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
            else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    // Called on logout and on new-user login to wipe the previous user's local data
    suspend fun clearAllLocalData() {
        childRepository.clearLocalData()
        measurementRepository.clearLocalData()
        getSharedPreferences("sabicare_prefs_simple", MODE_PRIVATE)
            .edit()
            .remove("user_avatar_uri")
            .remove("user_display_name")
            .remove("user_email")
            .apply()
    }
}