package com.example.sabicare_j.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.ActivityOnboardingBinding
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.profile.AddChildFragment

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment_container, AddChildFragment.newInstance(isOnboarding = true))
                .commit()
        }
    }

    fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}