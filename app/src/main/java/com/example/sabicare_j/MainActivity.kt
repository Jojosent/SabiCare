package com.example.sabicare_j.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sabicare_j.R
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.databinding.ActivityMainBinding
import com.example.sabicare_j.ui.onboarding.OnboardingActivity
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.utils.LocaleHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val childViewModel: ChildViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLocale(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkFirstLaunch()
        setupNavigation()
    }

    private fun checkFirstLaunch() {
        val repo = (application as SabiCareApplication).childRepository
        val prefs = getSharedPreferences("sabicare_prefs_simple", MODE_PRIVATE)

        lifecycleScope.launch {
            val childCount = repo.getChildCount()
            val isFirstLaunch = prefs.getBoolean("first_launch_complete", false)

            // Only show onboarding on true first launch
            if (!isFirstLaunch) {
                // Mark first launch as complete
                prefs.edit().putBoolean("first_launch_complete", true).apply()

                // Only go to onboarding if no children exist
                if (childCount == 0) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                }
            }
            // If first launch is complete and child count > 0, continue normally
            // If first launch is complete but childCount == 0, user skipped onboarding - let them add later
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.trackerFragment,
                R.id.resultsFragment,
                R.id.profileFragment -> showBottomNav()
                else -> hideBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        binding.bottomNavigation.animate().translationY(0f).setDuration(200).start()
    }

    private fun hideBottomNav() {
        binding.bottomNavigation.animate()
            .translationY(binding.bottomNavigation.height.toFloat())
            .setDuration(200).start()
    }
}