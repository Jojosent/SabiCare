package com.example.sabicare_j.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.sabicare_j.R
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.ui.auth.LoginActivity
import com.example.sabicare_j.ui.components.IconBadge
import com.example.sabicare_j.utils.LocaleHelper
import com.example.sabicare_j.utils.NotificationScheduler
import com.example.sabicare_j.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private const val PREF = "sabicare_prefs_simple"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREF, Context.MODE_PRIVATE) }

    var theme by remember { mutableStateOf(prefs.getString("theme", "system") ?: "system") }
    var language by remember { mutableStateOf(prefs.getString("language", LocaleHelper.DEFAULT_LANG) ?: LocaleHelper.DEFAULT_LANG) }
    var notifEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }
    var reminderNotif by remember { mutableStateOf(prefs.getBoolean("reminder_notif_enabled", true)) }
    var growthAlerts by remember { mutableStateOf(prefs.getBoolean("growth_alerts_enabled", true)) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentName by remember {
        mutableStateOf(prefs.getString("user_display_name", "") ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Group(stringResource(R.string.settings_account))
            SettingRow(
                icon = Icons.Filled.Email,
                title = stringResource(R.string.settings_name),
                subtitle = currentName.ifBlank { stringResource(R.string.not_set) },
                onClick = { showNameDialog = true }
            )

            Group(stringResource(R.string.settings_appearance))
            SettingRow(
                icon = Icons.Filled.DarkMode,
                title = stringResource(R.string.settings_theme),
                subtitle = themeLabel(theme),
                onClick = { showThemeDialog = true }
            )
            SettingRow(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.settings_language),
                subtitle = langLabel(language),
                onClick = { showLangDialog = true }
            )

            Group(stringResource(R.string.settings_notifications))
            SwitchRow(
                icon = Icons.Filled.NotificationsActive,
                title = stringResource(R.string.notifications_all),
                subtitle = stringResource(R.string.notifications_all_subtitle),
                checked = notifEnabled,
                onCheck = {
                    notifEnabled = it
                    prefs.edit { putBoolean("notifications_enabled", it) }
                    if (it) NotificationScheduler.schedule(context)
                    else NotificationScheduler.cancel(context)
                }
            )
            SwitchRow(
                icon = Icons.Filled.NotificationsActive,
                title = stringResource(R.string.notifications_reminders),
                subtitle = stringResource(R.string.notifications_reminders_subtitle),
                checked = reminderNotif,
                enabled = notifEnabled,
                onCheck = {
                    reminderNotif = it
                    prefs.edit { putBoolean("reminder_notif_enabled", it) }
                }
            )
            SwitchRow(
                icon = Icons.Filled.NotificationsActive,
                title = stringResource(R.string.notifications_growth),
                subtitle = stringResource(R.string.notifications_growth_subtitle),
                checked = growthAlerts,
                enabled = notifEnabled,
                onCheck = {
                    growthAlerts = it
                    prefs.edit { putBoolean("growth_alerts_enabled", it) }
                }
            )

            Group(stringResource(R.string.settings_privacy))
            SettingRow(
                icon = Icons.Filled.PrivacyTip,
                title = stringResource(R.string.privacy_policy),
                subtitle = "sabicare.app/privacy",
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://sabicare.app/privacy"))
                        )
                    }
                }
            )
            SettingRow(
                icon = Icons.Filled.DeleteForever,
                title = stringResource(R.string.delete_account),
                subtitle = stringResource(R.string.delete_account_subtitle),
                tintError = true,
                onClick = { showDeleteDialog = true }
            )

            Group(stringResource(R.string.settings_support))
            SettingRow(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.about_app),
                subtitle = stringResource(R.string.version_label),
                onClick = { showAboutDialog = true }
            )
            SettingRow(
                icon = Icons.Filled.Email,
                title = stringResource(R.string.feedback_title),
                subtitle = "support@sabicare.app",
                onClick = {
                    runCatching {
                        val i = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@sabicare.app")
                            putExtra(Intent.EXTRA_SUBJECT, "SabiCare 1.0 — Feedback")
                        }
                        context.startActivity(Intent.createChooser(i, "Email"))
                    }
                }
            )
            SettingRow(
                icon = Icons.Filled.Star,
                title = stringResource(R.string.rate_app),
                subtitle = stringResource(R.string.rate_app_on_play),
                onClick = {
                    val market = "market://details?id=${context.packageName}"
                    val play = "https://play.google.com/store/apps/details?id=${context.packageName}"
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(market))) }
                        .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(play))) }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showThemeDialog) {
        ChoiceDialog(
            title = stringResource(R.string.choose_theme),
            options = listOf(
                "light" to stringResource(R.string.theme_light),
                "dark" to stringResource(R.string.theme_dark),
                "system" to stringResource(R.string.theme_system)
            ),
            selected = theme,
            onPick = {
                theme = it
                prefs.edit { putString("theme", it) }
                ThemeManager.setTheme(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLangDialog) {
        ChoiceDialog(
            title = stringResource(R.string.choose_language),
            options = listOf(
                "kk" to stringResource(R.string.lang_kk),
                "ru" to stringResource(R.string.lang_ru),
                "en" to stringResource(R.string.lang_en)
            ),
            selected = language,
            onPick = { code ->
                language = code
                LocaleHelper.saveLocale(context, code)
                showLangDialog = false
                // Recreate activity so locale applies
                (context as? androidx.activity.ComponentActivity)?.recreate()
            },
            onDismiss = { showLangDialog = false }
        )
    }

    if (showNameDialog) {
        var temp by remember { mutableStateOf(currentName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.change_name_title)) },
            text = {
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text(stringResource(R.string.your_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (temp.isNotBlank()) {
                        currentName = temp.trim()
                        prefs.edit { putString("user_display_name", currentName) }
                        Toast.makeText(context, context.getString(R.string.saved_short), Toast.LENGTH_SHORT).show()
                    }
                    showNameDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_app)) },
            text = { Text(stringResource(R.string.about_body)) },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account)) },
            text = { Text(stringResource(R.string.delete_account_confirm)) },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    val activity = context as? androidx.activity.ComponentActivity
                    activity?.lifecycleScope?.launch {
                        runCatching {
                            (activity.application as SabiCareApplication).clearAllLocalData()
                        }
                        val auth = FirebaseAuth.getInstance()
                        auth.currentUser?.delete()
                            ?.addOnSuccessListener {
                                auth.signOut()
                                val intent = Intent(activity, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                activity.startActivity(intent)
                                activity.finish()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(
                                    activity,
                                    activity.getString(R.string.reauth_required, it.message ?: ""),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun Group(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tintError: Boolean = false,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tint = if (tintError) cs.error else cs.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(background = if (tintError) cs.errorContainer else cs.primaryContainer) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheck: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(background = cs.primaryContainer) {
                Icon(icon, contentDescription = null, tint = cs.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheck, enabled = enabled)
        }
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = code == selected, onClick = { onPick(code) })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

@Composable
private fun themeLabel(t: String) = when (t) {
    "light" -> stringResource(R.string.theme_light)
    "dark" -> stringResource(R.string.theme_dark)
    else -> stringResource(R.string.theme_system)
}

@Composable
private fun langLabel(l: String) = when (l) {
    "kk" -> stringResource(R.string.lang_kk)
    "ru" -> stringResource(R.string.lang_ru)
    "en" -> stringResource(R.string.lang_en)
    else -> l
}
