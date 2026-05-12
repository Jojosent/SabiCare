package com.example.sabicare_j.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.components.IconBadge
import com.example.sabicare_j.ui.components.OnResumeEffect
import com.example.sabicare_j.ui.components.SectionHeader
import com.example.sabicare_j.ui.components.TagPill
import com.example.sabicare_j.ui.content.Articles
import com.example.sabicare_j.ui.content.Medication
import com.example.sabicare_j.ui.home.HomeViewModel
import com.example.sabicare_j.ui.home.ReminderItem
import com.example.sabicare_j.ui.shared.ChildViewModel
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    childViewModel: ChildViewModel,
    onAddChildClick: () -> Unit,
    onTrackerClick: () -> Unit,
    onAddEntryClick: (String?) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val activeChild by childViewModel.activeChild.observeAsState()
    val reminders by homeViewModel.reminders.observeAsState(emptyList())
    val context = LocalContext.current

    // Refresh reminders whenever active child changes
    LaunchedEffect(activeChild?.id) {
        activeChild?.id?.let { homeViewModel.loadRemindersForChild(it) }
    }
    OnResumeEffect {
        activeChild?.id?.let { homeViewModel.loadRemindersForChild(it) }
    }

    val openUrl: (String) -> Unit = { url ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ───────── HEADER / CHILD CARD ─────────
        item {
            GreetingCard(
                child = activeChild,
                ageString = activeChild?.let { homeViewModel.getAgeString(it.birthDate) },
                onAddChild = onAddChildClick
            )
        }

        // ───────── QUICK ACTIONS ─────────
        item { Spacer(Modifier.height(16.dp)) }
        item {
            QuickActionsRow(
                onAddEntry = { onAddEntryClick(null) },
                onTracker = onTrackerClick,
                onAddChild = onAddChildClick
            )
        }

        // ───────── REMINDERS ─────────
        item { Spacer(Modifier.height(20.dp)) }
        item {
            val hasDue = reminders.any { it.isDue }
            SectionHeader(
                title = stringResource(R.string.reminders_title),
                subtitle = if (hasDue)
                    stringResource(R.string.reminders_today_due)
                else stringResource(R.string.reminders_all_set)
            )
        }
        if (reminders.isEmpty()) {
            item { EmptyReminders() }
        } else {
            items(reminders) { reminder ->
                ReminderCard(reminder) { onAddEntryClick(reminder.type.name) }
            }
        }

        // ───────── FEATURED ARTICLES (big hero) ─────────
        item { Spacer(Modifier.height(24.dp)) }
        item { SectionHeader(title = stringResource(R.string.featured_article_title), subtitle = stringResource(R.string.featured_article_subtitle)) }
        items(Articles.featured) { article ->
            HeroArticleCard(article = article, onClick = { openUrl(article.articleUrl) })
        }

        // ───────── ALL ARTICLES (horizontal scroll) ─────────
        item { Spacer(Modifier.height(16.dp)) }
        item { SectionHeader(title = stringResource(R.string.useful_articles), subtitle = stringResource(R.string.articles_subtitle)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(Articles.all) { article ->
                    ArticleCardSmall(article = article, onClick = { openUrl(article.articleUrl) })
                }
            }
        }

        // ───────── MEDICATIONS / VITAMINS ─────────
        item { Spacer(Modifier.height(24.dp)) }
        item { SectionHeader(title = stringResource(R.string.medications_title), subtitle = stringResource(R.string.medications_subtitle)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(Articles.medications) { med ->
                    MedicationCard(med = med, onClick = { openUrl(med.sourceUrl) })
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/* ───────────────────────── Components ───────────────────────── */

@Composable
private fun GreetingCard(
    child: ChildEntity?,
    ageString: String?,
    onAddChild: () -> Unit
) {
    val childName = child?.name
    val gender = child?.gender
    val brush = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.greeting),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(18.dp))

            if (childName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (!child?.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = child?.photoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop  // ← Crops image to circle
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (gender == "MALE") "👦" else "👧",
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = childName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Row {
                            TagPill(
                                text = ageString.orEmpty(),
                                background = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            TagPill(
                                text = if (gender == "MALE") stringResource(R.string.boy) else stringResource(R.string.girl),
                                background = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White
                            )
                        }
                    }
                }
            } else {
                EmptyChildCard(onAddChild)
            }
        }
    }
}

@Composable
private fun EmptyChildCard(onAddChild: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddChild() },
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(background = Color.White.copy(alpha = 0.3f)) {
                Icon(Icons.Filled.ChildCare, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.add_child),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.add_child_subtitle),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddEntry: () -> Unit,
    onTracker: () -> Unit,
    onAddChild: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Add,
            label = stringResource(R.string.qa_add),
            iconBg = cs.primary,
            container = cs.primaryContainer,
            onClick = onAddEntry
        )
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Monitor,
            label = stringResource(R.string.qa_tracker),
            iconBg = cs.secondary,
            container = cs.secondaryContainer,
            onClick = onTracker
        )
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.ChildCare,
            label = stringResource(R.string.qa_child),
            iconBg = cs.tertiary,
            container = cs.tertiaryContainer,
            onClick = onAddChild
        )
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconBg: Color,
    container: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            IconBadge(background = iconBg, size = 38.dp) {
                Icon(icon, contentDescription = label, tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyReminders() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(background = MaterialTheme.colorScheme.primary, size = 36.dp) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.empty_reminders_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReminderCard(reminder: ReminderItem, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val statusColor = if (reminder.isDue) cs.error else cs.primary
    val statusBg = if (reminder.isDue) cs.errorContainer else cs.primaryContainer
    val (icon, label) = when (reminder.type) {
        MeasurementType.HEIGHT -> Icons.Filled.Straighten to stringResource(R.string.type_height)
        MeasurementType.WEIGHT -> Icons.Filled.MonitorWeight to stringResource(R.string.type_weight)
        MeasurementType.FEEDINGS_COUNT -> Icons.Filled.LocalDining to stringResource(R.string.type_feedings_short)
        MeasurementType.CALORIES -> Icons.Filled.Restaurant to stringResource(R.string.type_calories)
        MeasurementType.SLEEP_DURATION -> Icons.Filled.Bedtime to stringResource(R.string.type_sleep_duration)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(background = statusBg) {
                Icon(icon, contentDescription = null, tint = statusColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (reminder.isDue) stringResource(R.string.reminders_today_due)
                    else stringResource(R.string.status_next_in, reminder.daysUntilNext),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            TagPill(
                text = reminder.lastValueText,
                background = cs.surfaceVariant,
                contentColor = cs.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroArticleCard(article: com.example.sabicare_j.ui.content.Article, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(article.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TagPill(
                        text = article.category,
                        background = Color.White.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    TagPill(
                        text = stringResource(R.string.read_minutes, article.readMinutes),
                        background = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        article.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleCardSmall(article: com.example.sabicare_j.ui.content.Article, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                TagPill(text = article.category)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.minutes_short, article.readMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(med: Medication, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(med.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = med.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    med.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                TagPill(text = med.ageRange)
                Spacer(Modifier.height(6.dp))
                Text(
                    med.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 3
                )
            }
        }
    }
}

// helper to format age from millis when needed
@Suppress("unused")
private fun ageDays(birth: Long) = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - birth).toInt()