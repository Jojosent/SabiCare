package com.example.sabicare_j

import android.app.Application
import android.content.Context
import com.example.sabicare_j.data.local.AppDatabase
import com.example.sabicare_j.data.repository.ChildRepository
import com.example.sabicare_j.data.repository.MeasurementRepository
import com.example.sabicare_j.utils.LocaleHelper
import com.example.sabicare_j.utils.NotificationScheduler
import com.example.sabicare_j.utils.ThemeManager

class SabiCareApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val childRepository: ChildRepository by lazy { ChildRepository(database.childDao()) }
    val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepository(database.measurementDao())
    }

    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(this)
        NotificationScheduler.ensureChannels(this)
        // Schedule daily notification check (idempotent — KEEP policy)
        if (notificationsEnabled()) {
            NotificationScheduler.schedule(this)
        }
    }

    override fun attachBaseContext(base: Context) {
        val lang = LocaleHelper.getSavedLocale(base)
        super.attachBaseContext(LocaleHelper.applyLocale(base, lang))
    }

    private fun notificationsEnabled(): Boolean {
        val prefs = getSharedPreferences("sabicare_prefs_simple", MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
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
