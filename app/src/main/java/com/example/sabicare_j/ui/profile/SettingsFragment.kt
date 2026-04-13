package com.example.sabicare_j.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.sabicare_j.databinding.FragmentSettingsBinding
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.utils.LocaleHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentSettings()
        setupThemeSelector()
        setupLanguageSelector()
        setupNotificationsToggle()
        setupAbout()
    }

    private fun loadCurrentSettings() {
        val prefs = requireContext()
            .getSharedPreferences("sabicare_prefs_simple", android.content.Context.MODE_PRIVATE)

        // Show current theme
        val theme = prefs.getString("theme", "system") ?: "system"
        binding.tvCurrentTheme.text = when (theme) {
            "light" -> "Жарық"
            "dark" -> "Қараңғы"
            else -> "Жүйелік"
        }

        // Show current language
        val lang = prefs.getString("language", "ru") ?: "ru"
        binding.tvCurrentLanguage.text = when (lang) {
            "kk" -> "Қазақша"
            "en" -> "English"
            else -> "Русский"
        }

        // Notifications
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.switchNotifications.isChecked = notificationsEnabled
    }

    private fun setupThemeSelector() {
        val themes = arrayOf("🌞  Жарық", "🌙  Қараңғы", "📱  Жүйелік")
        val themeValues = arrayOf("light", "dark", "system")

        binding.itemTheme.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Тема таңдау")
                .setItems(themes) { _, which ->
                    val selectedTheme = themeValues[which]

                    // Save to prefs
                    requireContext()
                        .getSharedPreferences("sabicare_prefs_simple", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putString("theme", selectedTheme)
                        .apply()

                    // Apply immediately
                    val mode = when (selectedTheme) {
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.setDefaultNightMode(mode)

                    // Update label
                    binding.tvCurrentTheme.text = themes[which].trim().drop(2).trim()
                }
                .show()
        }
    }

    private fun setupLanguageSelector() {
        val langs = arrayOf("🇰🇿  Қазақша", "🇷🇺  Русский", "🇬🇧  English")
        val langCodes = arrayOf("kk", "ru", "en")

        binding.itemLanguage.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Тіл таңдау / Выбор языка")
                .setItems(langs) { _, which ->
                    val code = langCodes[which]

                    // Save
                    LocaleHelper.saveLocale(requireContext(), code)

                    // Restart activity to apply locale
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                .show()
        }
    }

    private fun setupNotificationsToggle() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            requireContext()
                .getSharedPreferences("sabicare_prefs_simple", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("notifications_enabled", isChecked)
                .apply()
        }
    }

    private fun setupAbout() {
        binding.itemAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("SabiCare туралы")
                .setMessage(
                    "Нұсқа: 1.0\n\n" +
                            "SabiCare — жаңа туылған нәрестелердің дамуын бақылауға арналған қосымша.\n\n" +
                            "Деректер ДДҰ стандарттары негізінде есептеледі.\n\n" +
                            "© 2024 SabiCare"
                )
                .setPositiveButton("Жабу", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}