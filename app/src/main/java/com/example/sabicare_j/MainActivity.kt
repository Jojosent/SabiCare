package com.example.sabicare_j.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.lifecycleScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.ui.SabiCareApp
import com.example.sabicare_j.ui.onboarding.OnboardingActivity
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.ui.theme.SabiCareTheme
import com.example.sabicare_j.utils.LocaleHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val childViewModel: ChildViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLocale(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkFirstLaunch()

        setContent {
            SabiCareTheme {
                SabiCareApp(childViewModel = childViewModel)
            }
        }
    }

    private fun checkFirstLaunch() {
        val repo = (application as SabiCareApplication).childRepository
        val prefs = getSharedPreferences("sabicare_prefs_simple", MODE_PRIVATE)

        lifecycleScope.launch {
            val childCount = repo.getChildCount()
            val isFirstLaunch = prefs.getBoolean("first_launch_complete", false)

            if (!isFirstLaunch) {
                prefs.edit().putBoolean("first_launch_complete", true).apply()
                if (childCount == 0) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                }
            }
        }
    }
}
