package com.example.sabicare_j.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.ui.tracker.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    childViewModel: ChildViewModel,
    initialType: String?,
    onBack: () -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeChild by childViewModel.activeChild.observeAsState()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    var selectedType by remember {
        mutableStateOf(
            runCatching { initialType?.let { MeasurementType.valueOf(it) } }.getOrNull()
                ?: MeasurementType.HEIGHT
        )
    }
    var valueText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_entry), fontWeight = FontWeight.SemiBold) },
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.parameter), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            FlowRowSimple {
                MeasurementType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(typeLabel(type)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it; error = null },
                label = { Text(stringResource(R.string.value_with_unit, selectedType.unit)) },
                placeholder = { Text(hintForType(selectedType)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                isError = error != null,
                supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = dateFormat.format(Date(dateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date)) },
                trailingIcon = {
                    IconButton(onClick = {
                        showDatePicker(context, dateMillis) { dateMillis = it }
                    }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker(context, dateMillis) { dateMillis = it }
                    }
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note_optional)) },
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val v = valueText.trim().toDoubleOrNull()
                    when {
                        valueText.isBlank() -> error = context.getString(R.string.enter_value)
                        v == null || v <= 0 -> error = context.getString(R.string.invalid_value)
                        activeChild == null -> error = context.getString(R.string.add_child_first)
                        else -> {
                            // Use the same Application-scoped repo via a fresh ViewModel
                            val vm = TrackerViewModel(context.applicationContext as Application)
                            vm.addMeasurement(
                                childId = activeChild!!.id,
                                type = selectedType,
                                value = v,
                                note = note.ifBlank { null },
                                recordedAt = dateMillis
                            )
                            Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun showDatePicker(context: android.content.Context, current: Long, onPick: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = current }
    DatePickerDialog(
        context,
        { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0); set(Calendar.MILLISECOND, 0) }
            onPick(c.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
}

@Composable
private fun typeLabel(t: MeasurementType): String = when (t) {
    MeasurementType.HEIGHT -> "📏 " + stringResource(R.string.type_height)
    MeasurementType.WEIGHT -> "⚖️ " + stringResource(R.string.type_weight)
    MeasurementType.FEEDINGS_COUNT -> "🍼 " + stringResource(R.string.type_feedings_short)
    MeasurementType.CALORIES -> "🔥 " + stringResource(R.string.type_calories)
    MeasurementType.SLEEP_DURATION -> "😴 " + stringResource(R.string.type_sleep_duration)
}

private fun hintForType(t: MeasurementType): String = when (t) {
    MeasurementType.HEIGHT -> "Мысалы: 52.5"
    MeasurementType.WEIGHT -> "Мысалы: 3500"
    MeasurementType.FEEDINGS_COUNT -> "Мысалы: 8"
    MeasurementType.CALORIES -> "Мысалы: 450"
    MeasurementType.SLEEP_DURATION -> "Мысалы: 840"
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowSimple(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { content() }
}
