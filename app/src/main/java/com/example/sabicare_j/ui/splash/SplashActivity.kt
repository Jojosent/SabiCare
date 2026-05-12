package com.example.sabicare_j.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sabicare_j.utils.LocaleHelper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sabicare_j.ui.auth.LoginActivity
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.theme.SabiCareTheme
import com.example.sabicare_j.ui.theme.Teal500
import com.example.sabicare_j.ui.theme.Indigo500
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLocale(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SabiCareTheme {
                SplashContent()
            }
        }
    }
}

@Composable
private fun SplashContent() {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        val intent = if (FirebaseAuth.getInstance().currentUser != null)
            Intent(context, MainActivity::class.java)
        else
            Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        (context as ComponentActivity).finish()
    }

    val gradient = Brush.linearGradient(listOf(Teal500, Indigo500))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = visible, enter = fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://play-lh.googleusercontent.com/OsLjx2TZ9gbvHVwYNasH0jGSHu6jYDhsv0r-g_NE18sAat35Os2uMPCUjh-jkW_MplY=w240-h480-rw",
                            contentDescription = null,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "SabiCare",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(com.example.sabicare_j.R.string.splash_subtitle),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
