package com.example.sabicare_j.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementEntity
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.components.IconBadge
import com.example.sabicare_j.ui.components.OnResumeEffect
import com.example.sabicare_j.ui.components.SectionHeader
import com.example.sabicare_j.ui.components.TagPill
import com.example.sabicare_j.ui.results.ResultCardState
import com.example.sabicare_j.ui.results.ResultsViewModel
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.utils.GrowthStandards
import com.example.sabicare_j.utils.PdfGenerator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    childViewModel: ChildViewModel,
    resultsViewModel: ResultsViewModel = viewModel()
) {
    val activeChild by childViewModel.activeChild.observeAsState()
    val cards by resultsViewModel.resultCards.observeAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(activeChild?.id) {
        activeChild?.let { resultsViewModel.loadForChild(it) }
    }
    OnResumeEffect {
        activeChild?.let { resultsViewModel.loadForChild(it) }
    }

    var showPdfDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (activeChild != null && cards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showPdfDialog = true },
                    icon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                    text = { Text(stringResource(R.string.export_pdf)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
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
                        stringResource(R.string.results_title),
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
                    title = stringResource(R.string.compare_who),
                    subtitle = stringResource(R.string.results_subtitle)
                )
            }
            items(cards) { card -> ResultCard(card) }
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
                            stringResource(R.string.results_empty),
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showPdfDialog && activeChild != null) {
        PdfRangeDialog(
            onDismiss = { showPdfDialog = false },
            onGenerate = { from, to ->
                showPdfDialog = false
                runCatching {
                    val file = PdfGenerator.generate(context, activeChild!!, cards, from, to)
                    PdfGenerator.share(context, file)
                }.onFailure {
                    Toast.makeText(context, context.getString(R.string.pdf_error, it.message ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
private fun ResultCard(card: ResultCardState) {
    val cs = MaterialTheme.colorScheme
    val (statusText, statusColor, statusBg) = when (card.status) {
        GrowthStandards.ComplianceStatus.NORMAL -> Triple(stringResource(R.string.status_normal), cs.primary, cs.primaryContainer)
        GrowthStandards.ComplianceStatus.BELOW -> Triple(stringResource(R.string.status_below_norm), Color(0xFFF59E0B), Color(0xFFFEF3C7))
        GrowthStandards.ComplianceStatus.ABOVE -> Triple(stringResource(R.string.status_above_norm), Color(0xFFF59E0B), Color(0xFFFEF3C7))
        GrowthStandards.ComplianceStatus.CRITICAL -> Triple(stringResource(R.string.status_critical), cs.error, cs.errorContainer)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(background = cs.primaryContainer, size = 40.dp) {
                    Text(emojiFor(card.type), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        labelFor(card.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = card.latestValue?.let { format(it) + " ${card.type.unit}" } ?: stringResource(R.string.status_no_record),
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurface
                    )
                }
                TagPill(statusText, background = statusBg, contentColor = statusColor)
            }
            card.norm?.let { norm ->
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(
                        R.string.norm_range_format,
                        format(norm.min),
                        format(norm.max),
                        card.type.unit,
                        format(norm.average)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (card.compliancePercent / 100f).coerceIn(0f, 1f),
                    color = statusColor,
                    trackColor = cs.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(cs.surfaceVariant, shape = RoundedCornerShape(50))
                )
                Text(
                    stringResource(R.string.compliance, card.compliancePercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
            }
            if (card.history.size >= 2) {
                Spacer(Modifier.height(12.dp))
                MiniSparkline(history = card.history, color = cs.primary)
            }
        }
    }
}

/** Tiny SVG-style line chart drawn with Canvas */
@Composable
private fun MiniSparkline(history: List<MeasurementEntity>, color: Color) {
    val values = history.map { it.value }
    val min = values.min()
    val max = values.max()
    val range = (max - min).coerceAtLeast(0.0001)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val w = size.width
        val h = size.height
        val n = values.size
        if (n < 2) return@Canvas
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = w * (i.toFloat() / (n - 1))
            val y = h - (((v - min) / range).toFloat() * h * 0.9f) - h * 0.05f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        // dot at end
        val last = values.last()
        val ex = w
        val ey = h - (((last - min) / range).toFloat() * h * 0.9f) - h * 0.05f
        drawCircle(color, radius = 6f, center = Offset(ex, ey))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfRangeDialog(onDismiss: () -> Unit, onGenerate: (Long, Long) -> Unit) {
    var range by remember { mutableStateOf(30) } // days
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pdf_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.pdf_dialog_subtitle), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 30, 90, 180, 365).forEach { d ->
                        FilterChip(
                            selected = range == d,
                            onClick = { range = d },
                            label = { Text(stringResource(R.string.days_n, d)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val to = System.currentTimeMillis()
                    val from = Calendar.getInstance().also {
                        it.timeInMillis = to
                        it.add(Calendar.DAY_OF_YEAR, -range)
                    }.timeInMillis
                    onGenerate(from, to)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.generate)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun labelFor(t: MeasurementType): String = when (t) {
    MeasurementType.HEIGHT -> stringResource(R.string.type_height)
    MeasurementType.WEIGHT -> stringResource(R.string.type_weight)
    MeasurementType.FEEDINGS_COUNT -> stringResource(R.string.type_feedings_count)
    MeasurementType.CALORIES -> stringResource(R.string.type_calories)
    MeasurementType.SLEEP_DURATION -> stringResource(R.string.type_sleep_duration)
}
private fun emojiFor(t: MeasurementType) = when (t) {
    MeasurementType.HEIGHT -> "📏"
    MeasurementType.WEIGHT -> "⚖️"
    MeasurementType.FEEDINGS_COUNT -> "🍼"
    MeasurementType.CALORIES -> "🔥"
    MeasurementType.SLEEP_DURATION -> "😴"
}

private fun format(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
