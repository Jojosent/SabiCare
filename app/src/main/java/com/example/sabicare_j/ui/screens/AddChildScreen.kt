package com.example.sabicare_j.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.ui.shared.ChildViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(
    childViewModel: ChildViewModel,
    editChildId: Long?,
    isOnboarding: Boolean,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val allChildren by childViewModel.allChildren.observeAsState(emptyList())

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthMillis by remember { mutableStateOf(0L) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Copy to app-internal storage so the URI survives process death.
            val saved = runCatching {
                val file = java.io.File(
                    context.filesDir,
                    "child_${System.currentTimeMillis()}.jpg"
                )
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { out -> input.copyTo(out) }
                }
                file.absolutePath
            }.getOrNull()
            photoUri = saved ?: uri.toString()
        }
    }

    // Prefill when editing
    LaunchedEffect(editChildId, allChildren) {
        if (editChildId != null) {
            allChildren.firstOrNull { it.id == editChildId }?.let { c ->
                name = c.name
                gender = c.gender
                birthMillis = c.birthDate
                photoUri = c.photoUri
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            isOnboarding -> stringResource(R.string.onboarding_welcome)
                            editChildId != null -> stringResource(R.string.edit_child)
                            else -> stringResource(R.string.add_child)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (!isOnboarding) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
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
            if (isOnboarding) {
                Text(
                    stringResource(R.string.onboarding_first_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.onboarding_first_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }

            // Photo picker (optional)
            PhotoPicker(
                photoUri = photoUri,
                onPick = {
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemove = { photoUri = null }
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text(stringResource(R.string.child_name)) },
                placeholder = { Text(stringResource(R.string.name_hint)) },
                shape = RoundedCornerShape(14.dp),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = if (birthMillis == 0L) "" else dateFormat.format(Date(birthMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.child_birth_date)) },
                trailingIcon = {
                    IconButton(onClick = {
                        pickDate(context, birthMillis) {
                            birthMillis = it; dateError = null
                        }
                    }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null)
                    }
                },
                isError = dateError != null,
                supportingText = { dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        pickDate(context, birthMillis) { birthMillis = it; dateError = null }
                    }
            )

            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.child_gender),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = gender == "MALE",
                    onClick = { gender = "MALE" },
                    label = { Text(stringResource(R.string.gender_male)) }
                )
                FilterChip(
                    selected = gender == "FEMALE",
                    onClick = { gender = "FEMALE" },
                    label = { Text(stringResource(R.string.gender_female)) }
                )
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    var ok = true
                    if (name.isBlank()) { nameError = context.getString(R.string.error_name_required); ok = false }
                    if (birthMillis == 0L) { dateError = context.getString(R.string.error_date_required); ok = false }
                    if (gender.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.select_gender), Toast.LENGTH_SHORT).show()
                        ok = false
                    }
                    if (!ok) return@Button

                    val entity = ChildEntity(
                        id = editChildId ?: 0L,
                        name = name.trim(),
                        birthDate = birthMillis,
                        gender = gender,
                        photoUri = photoUri,
                        isActive = true
                    )
                    if (editChildId != null) childViewModel.updateChild(entity)
                    else childViewModel.addChild(entity)
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        isOnboarding -> stringResource(R.string.start)
                        editChildId != null -> stringResource(R.string.save)
                        else -> stringResource(R.string.add_child)
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isOnboarding) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.add_later)) }
            }
        }
    }
}

@Composable
private fun PhotoPicker(
    photoUri: String?,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(cs.primaryContainer)
                .clickable { onPick() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri.isNullOrBlank()) {
                Icon(
                    Icons.Filled.ChildCare,
                    contentDescription = null,
                    tint = cs.onPrimaryContainer
                )
            } else {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop  // ← Crops image to circle
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.child_photo_optional),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPick) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.pick_photo))
                }
                if (!photoUri.isNullOrBlank()) {
                    TextButton(onClick = onRemove) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.remove_photo))
                    }
                }
            }
        }
    }
}

private fun pickDate(context: android.content.Context, current: Long, onPick: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply {
        if (current > 0) timeInMillis = current
    }
    DatePickerDialog(
        context,
        { _, y, m, d ->
            val c = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }
            onPick(c.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
}