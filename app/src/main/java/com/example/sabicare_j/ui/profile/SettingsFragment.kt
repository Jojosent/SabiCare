package com.example.sabicare_j.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.FragmentSettingsBinding
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.utils.LocaleHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()

    // ── Avatar picker ──────────────────────────────────────────────────────
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                saveAvatarUri(uri)
                loadAvatar(uri.toString())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentValues()
        setupAvatarSection()
        setupAccountSection()
        setupAppearanceSection()
        setupNotificationsSection()
        setupPrivacySection()
        setupSupportSection()
        setupSignOut()
    }

    // ── Load saved prefs ───────────────────────────────────────────────────
    private fun loadCurrentValues() {
        val prefs = requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)

        // Avatar
        val avatarUri = prefs.getString("user_avatar_uri", null)
        loadAvatar(avatarUri)

        // Display name / email
        val user = auth.currentUser
        binding.tvUserEmail.text = user?.email ?: prefs.getString("user_email", "—")
        binding.tvUserName.text = user?.displayName
            ?: prefs.getString("user_display_name", "Пайдаланушы")

        // Theme
        val theme = prefs.getString("theme", "system") ?: "system"
        binding.tvCurrentTheme.text = when (theme) {
            "light" -> "Жарық"
            "dark" -> "Қараңғы"
            else -> "Жүйелік"
        }

        // Language
        val lang = prefs.getString("language", "ru") ?: "ru"
        binding.tvCurrentLanguage.text = when (lang) {
            "kk" -> "Қазақша"
            "en" -> "English"
            else -> "Русский"
        }

        // Notifications
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        binding.switchReminderNotif.isChecked = prefs.getBoolean("reminder_notif_enabled", true)
        binding.switchGrowthAlerts.isChecked = prefs.getBoolean("growth_alerts_enabled", true)
    }

    // ── Avatar ─────────────────────────────────────────────────────────────
    private fun setupAvatarSection() {
        binding.ivUserAvatar.setOnClickListener { pickAvatar() }
        binding.btnChangeAvatar.setOnClickListener { pickAvatar() }
    }

    private fun pickAvatar() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun saveAvatarUri(uri: Uri) {
        // Persist read permission across reboots
        requireContext().contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE).edit {
            putString("user_avatar_uri", uri.toString())
        }
    }

    private fun loadAvatar(uriString: String?) {
        if (uriString != null) {
            Glide.with(this)
                .load(Uri.parse(uriString))
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .into(binding.ivUserAvatar)
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    // ── Account ────────────────────────────────────────────────────────────
    private fun setupAccountSection() {
        binding.itemChangeName.setOnClickListener { showChangeNameDialog() }
        binding.itemChangeEmail.setOnClickListener { showChangeEmailDialog() }
        binding.itemChangePassword.setOnClickListener { showChangePasswordDialog() }
    }

    private fun showChangeNameDialog() {
        val prefs = requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
        val currentName = prefs.getString("user_display_name", "") ?: ""

        val dialogView = layoutInflater.inflate(R.layout.dialog_text_input, null)
        val til = dialogView.findViewById<TextInputLayout>(R.id.til_dialog_input)
        val et = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_input)
        til.hint = "Аты"
        et.setText(currentName)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Атыңызды өзгерту")
            .setView(dialogView)
            .setPositiveButton("Сақтау") { _, _ ->
                val newName = et.text?.toString()?.trim() ?: return@setPositiveButton
                if (newName.isBlank()) return@setPositiveButton
                prefs.edit { putString("user_display_name", newName) }
                binding.tvUserName.text = newName
                Toast.makeText(requireContext(), "Аты жаңартылды", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun showChangeEmailDialog() {
        val user = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "Кіру қажет", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val etCurrentPass = dialogView.findViewById<TextInputEditText>(R.id.et_current_password)
        val etNewEmail = dialogView.findViewById<TextInputEditText>(R.id.et_new_email)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Email өзгерту")
            .setView(dialogView)
            .setPositiveButton("Жаңарту") { _, _ ->
                val currentPass = etCurrentPass.text?.toString() ?: return@setPositiveButton
                val newEmail = etNewEmail.text?.toString()?.trim() ?: return@setPositiveButton
                if (currentPass.isBlank() || newEmail.isBlank()) return@setPositiveButton

                val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updateEmail(newEmail)
                            .addOnSuccessListener {
                                binding.tvUserEmail.text = newEmail
                                requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
                                    .edit { putString("user_email", newEmail) }
                                Toast.makeText(requireContext(), "Email жаңартылды ✓", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Қате: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Қате құпия сөз", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val user = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "Кіру қажет", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<TextInputEditText>(R.id.et_current_password)
        val etNew = dialogView.findViewById<TextInputEditText>(R.id.et_new_password)
        val etConfirm = dialogView.findViewById<TextInputEditText>(R.id.et_confirm_password)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Құпия сөзді өзгерту")
            .setView(dialogView)
            .setPositiveButton("Жаңарту") { _, _ ->
                val currentPass = etCurrent.text?.toString() ?: return@setPositiveButton
                val newPass = etNew.text?.toString() ?: return@setPositiveButton
                val confirmPass = etConfirm.text?.toString() ?: return@setPositiveButton

                if (newPass != confirmPass) {
                    Toast.makeText(requireContext(), "Құпия сөздер сәйкес келмейді", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass.length < 6) {
                    Toast.makeText(requireContext(), "Кемінде 6 таңба болуы керек", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPass)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Құпия сөз жаңартылды ✓", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Қате: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Қате ағымдағы құпия сөз", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    // ── Appearance ─────────────────────────────────────────────────────────
    private fun setupAppearanceSection() {
        setupThemeSelector()
        setupLanguageSelector()
    }

    private fun setupThemeSelector() {
        val themes = arrayOf("🌞  Жарық", "🌙  Қараңғы", "📱  Жүйелік")
        val themeValues = arrayOf("light", "dark", "system")

        binding.itemTheme.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Тема таңдау")
                .setItems(themes) { _, which ->
                    val selectedTheme = themeValues[which]
                    requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)
                        .edit { putString("theme", selectedTheme) }
                    val mode = when (selectedTheme) {
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.setDefaultNightMode(mode)
                    binding.tvCurrentTheme.text = themes[which].drop(3)
                }
                .show()
        }
    }

    private fun setupLanguageSelector() {
        val langs = arrayOf("🇰🇿  Қазақша", "🇷🇺  Русский", "🇬🇧  English")
        val langCodes = arrayOf("kk", "ru", "en")

        binding.itemLanguage.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Тіл таңдау")
                .setItems(langs) { _, which ->
                    LocaleHelper.saveLocale(requireContext(), langCodes[which])
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                .show()
        }
    }

    // ── Notifications ──────────────────────────────────────────────────────
    private fun setupNotificationsSection() {
        val prefs = requireContext().getSharedPreferences("sabicare_prefs_simple", Context.MODE_PRIVATE)

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("notifications_enabled", checked) }
            // Cascade — disable sub-switches visually when master is off
            binding.switchReminderNotif.isEnabled = checked
            binding.switchGrowthAlerts.isEnabled = checked
        }
        binding.switchReminderNotif.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("reminder_notif_enabled", checked) }
        }
        binding.switchGrowthAlerts.setOnCheckedChangeListener { _, checked ->
            prefs.edit { putBoolean("growth_alerts_enabled", checked) }
        }
    }

    // ── Privacy / Security ─────────────────────────────────────────────────
    private fun setupPrivacySection() {
        binding.itemDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Аккаунтты жою")
                .setMessage("Барлық деректер, оның ішінде балалар профилі мен өлшемдер жойылады. Бұл іс-әрекетті қайтару мүмкін емес.")
                .setPositiveButton("Жою") { _, _ ->
                    auth.currentUser?.delete()
                        ?.addOnSuccessListener {
                            Toast.makeText(requireContext(), "Аккаунт жойылды", Toast.LENGTH_SHORT).show()
                            // Navigate to onboarding / login
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Алдымен қайта кіріңіз: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Бас тарту", null)
                .show()
        }

        binding.itemPrivacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sabicare.app/privacy"))
            startActivity(intent)
        }
    }

    // ── Support ────────────────────────────────────────────────────────────
    private fun setupSupportSection() {
        binding.itemAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("SabiCare туралы")
                .setMessage("Нұсқа: 1.0.0\n\nSabiCare — жаңа туылған нәрестелердің дамуын бақылауға арналған қосымша.\n\nДеректер ДДҰ (WHO) стандарттары негізінде есептеледі.\n\n© 2024 SabiCare")
                .setPositiveButton("Жабу", null)
                .show()
        }

        binding.itemFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@sabicare.app")
                putExtra(Intent.EXTRA_SUBJECT, "SabiCare 1.0 — Пікір / Feedback")
            }
            startActivity(Intent.createChooser(intent, "Email жіберу"))
        }

        binding.itemRateApp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${requireContext().packageName}")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
            }
        }
    }

    // ── Sign out ───────────────────────────────────────────────────────────
    private fun setupSignOut() {
        binding.btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Шығу")
                .setMessage("Есептік жазбадан шығасыз ба?")
                .setPositiveButton("Шығу") { _, _ ->
                    auth.signOut()
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Бас тарту", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}