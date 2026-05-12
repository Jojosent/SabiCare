package com.example.sabicare_j.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.onboarding.OnboardingActivity
import com.example.sabicare_j.ui.theme.Indigo500
import com.example.sabicare_j.ui.theme.SabiCareTheme
import com.example.sabicare_j.ui.theme.Teal500
import com.example.sabicare_j.utils.LocaleHelper
import androidx.compose.ui.res.stringResource
import com.example.sabicare_j.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLocale(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContent {
            SabiCareTheme {
                LoginContent(
                    onLoginSuccess = ::goToMain,
                    onRegisterSuccess = ::goToOnboarding
                )
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }
}

@Composable
private fun LoginContent(
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val gradient = Brush.linearGradient(listOf(Teal500, Indigo500))

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.app_name),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.splash_subtitle),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        TabPill(
                            stringResource(R.string.login),
                            selected = isLogin,
                            modifier = Modifier.weight(1f),
                            onClick = { isLogin = true }
                        )
                        TabPill(
                            stringResource(R.string.register),
                            selected = !isLogin,
                            modifier = Modifier.weight(1f),
                            onClick = { isLogin = false }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    if (!isLogin) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.your_name)) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isLogin) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            label = { Text(stringResource(R.string.confirm_password)) },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (loading) return@Button
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isLogin) {
                                if (name.isBlank()) {
                                    Toast.makeText(context, context.getString(R.string.enter_your_name), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (password != confirm) {
                                    Toast.makeText(context, context.getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (password.length < 6) {
                                    Toast.makeText(context, context.getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            loading = true
                            if (isLogin) {
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnSuccessListener { result ->
                                        val uid = result.user?.uid ?: ""
                                        val prefs = context.getSharedPreferences(
                                            "sabicare_prefs_simple",
                                            android.content.Context.MODE_PRIVATE
                                        )
                                        activity.lifecycleScope.launch {
                                            if (prefs.getString("last_logged_uid", "") != uid) {
                                                runCatching {
                                                    (activity.application as SabiCareApplication).clearAllLocalData()
                                                }
                                            }
                                            prefs.edit().putString("last_logged_uid", uid).apply()
                                            loading = false
                                            onLoginSuccess()
                                        }
                                    }
                                    .addOnFailureListener {
                                        loading = false
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_with_message, it.localizedMessage ?: ""),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnSuccessListener { result ->
                                        val uid = result.user!!.uid
                                        val data = hashMapOf(
                                            "name" to name.trim(),
                                            "email" to email.trim(),
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        db.collection("users").document(uid).set(data)
                                            .addOnSuccessListener {
                                                activity.lifecycleScope.launch {
                                                    runCatching {
                                                        (activity.application as SabiCareApplication).clearAllLocalData()
                                                    }
                                                    context.getSharedPreferences(
                                                        "sabicare_prefs_simple",
                                                        android.content.Context.MODE_PRIVATE
                                                    ).edit()
                                                        .putString("last_logged_uid", uid)
                                                        .putString("user_display_name", name.trim())
                                                        .putString("user_email", email.trim())
                                                        .apply()
                                                    loading = false
                                                    onRegisterSuccess()
                                                }
                                            }
                                            .addOnFailureListener {
                                                loading = false
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.error_with_message, it.localizedMessage ?: ""),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        loading = false
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_with_message, it.localizedMessage ?: ""),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                if (isLogin) stringResource(R.string.login) else stringResource(R.string.register),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
