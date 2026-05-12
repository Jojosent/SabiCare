package com.example.sabicare_j.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sabicare_j.R
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.screens.AddChildScreen
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.ui.theme.Indigo500
import com.example.sabicare_j.ui.theme.SabiCareTheme
import com.example.sabicare_j.ui.theme.Teal500
import com.example.sabicare_j.utils.LocaleHelper

class OnboardingActivity : ComponentActivity() {

    private val childViewModel: ChildViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLocale(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SabiCareTheme {
                OnboardingFlow(
                    childViewModel = childViewModel,
                    onFinish = ::finishOnboarding
                )
            }
        }
    }

    fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

private data class OnboardPage(val title: String, val subtitle: String, val image: String)

private val pageImages = listOf(
    "https://play-lh.googleusercontent.com/OsLjx2TZ9gbvHVwYNasH0jGSHu6jYDhsv0r-g_NE18sAat35Os2uMPCUjh-jkW_MplY=w240-h480-rw",
    "https://images.unsplash.com/photo-1492725764893-90b379c2b6e7?w=900&q=80",
    "https://alldaymedicalcare.com/wp-content/uploads/2023/08/2424258.webp"
)

@Composable
private fun OnboardingFlow(childViewModel: ChildViewModel, onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardPage(stringResource(R.string.ob_title_1), stringResource(R.string.ob_subtitle_1), pageImages[0]),
        OnboardPage(stringResource(R.string.ob_title_2), stringResource(R.string.ob_subtitle_2), pageImages[1]),
        OnboardPage(stringResource(R.string.ob_title_3), stringResource(R.string.ob_subtitle_3), pageImages[2])
    )
    val totalIntroPages = pages.size

    if (step < totalIntroPages) {
        IntroPage(
            page = pages[step],
            index = step,
            count = totalIntroPages,
            onNext = { step++ },
            onSkip = { step = totalIntroPages }
        )
    } else {
        AddChildScreen(
            childViewModel = childViewModel,
            editChildId = null,
            isOnboarding = true,
            onDone = onFinish,
            onBack = { step = totalIntroPages - 1 }
        )
    }
}

@Composable
private fun IntroPage(
    page: OnboardPage,
    index: Int,
    count: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val gradient = Brush.linearGradient(listOf(Teal500, Indigo500))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero image with gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                AsyncImage(
                    model = page.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SabiCare",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.skip), color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                page.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 36.dp)
            )

            Spacer(Modifier.weight(1f))

            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                repeat(count) { i ->
                    Box(
                        modifier = Modifier
                            .size(width = if (i == index) 24.dp else 8.dp, height = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (i == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (index == count - 1) stringResource(R.string.start) else stringResource(R.string.next),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
