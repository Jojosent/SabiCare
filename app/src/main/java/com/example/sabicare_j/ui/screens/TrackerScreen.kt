package com.example.sabicare_j.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.components.IconBadge
import com.example.sabicare_j.ui.components.OnResumeEffect
import com.example.sabicare_j.ui.components.SectionHeader
import com.example.sabicare_j.ui.components.TagPill
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.ui.tracker.MeasurementCardState
import com.example.sabicare_j.ui.tracker.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    childViewModel: ChildViewModel,
    onAddEntry: (String?) -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    val activeChild by childViewModel.activeChild.observeAsState()
    val cards by trackerViewModel.cards.observeAsState(emptyList())

    LaunchedEffect(activeChild?.id) {
        activeChild?.id?.let { trackerViewModel.loadForChild(it) }
    }
    // Refresh on resume so newly-saved entries appear immediately
    OnResumeEffect {
        activeChild?.id?.let { trackerViewModel.refreshCards(it) }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddEntry(null) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_entry)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        stringResource(R.string.tracker_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        activeChild?.name ?: stringResource(R.string.select_child),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                SectionHeader(
                    title = stringResource(R.string.all_parameters),
                    subtitle = stringResource(R.string.tap_card_to_add)
                )
            }
            items(cards) { card ->
                MeasurementCard(card) { onAddEntry(card.type.name) }
            }
            if (cards.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            stringResource(R.string.please_add_child),
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(state: MeasurementCardState, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val (icon, name) = iconAndName(state.type)
    val statusText = when {
        state.latestValue == null -> stringResource(R.string.status_no_record)
        state.isDue -> stringResource(R.string.status_due_now)
        state.daysUntilNext < 0 -> stringResource(R.string.status_overdue, -state.daysUntilNext)
        else -> stringResource(R.string.status_next_in, state.daysUntilNext)
    }
    val isAlert = state.isDue || (state.daysUntilNext < 0 && state.latestValue != null)
    val statusColor = if (isAlert) cs.error else cs.primary
    val statusBg = if (isAlert) cs.errorContainer else cs.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(background = cs.primaryContainer, size = 48.dp) {
                Icon(icon, contentDescription = null, tint = cs.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (state.latestValue != null)
                        formatValue(state.latestValue) + " ${state.type.unit}"
                    else "—",
                    style = MaterialTheme.typography.bodyMedium
                )
                state.latestDateMillis?.let {
                    Text(
                        text = stringResource(R.string.last_measured, dateFormat.format(Date(it))),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
            TagPill(
                text = statusText,
                background = statusBg,
                contentColor = statusColor
            )
        }
    }
}

private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

private fun formatValue(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)

@Composable
private fun iconAndName(t: MeasurementType): Pair<ImageVector, String> = when (t) {
    MeasurementType.HEIGHT -> Icons.Filled.Straighten to stringResource(R.string.type_height)
    MeasurementType.WEIGHT -> Icons.Filled.MonitorWeight to stringResource(R.string.type_weight)
    MeasurementType.FEEDINGS_COUNT -> Icons.Filled.LocalDining to stringResource(R.string.type_feedings_count)
    MeasurementType.CALORIES -> Icons.Filled.Restaurant to stringResource(R.string.type_calories)
    MeasurementType.SLEEP_DURATION -> Icons.Filled.Bedtime to stringResource(R.string.type_sleep_duration)
}
