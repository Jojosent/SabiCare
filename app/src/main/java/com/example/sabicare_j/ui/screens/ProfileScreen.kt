package com.example.sabicare_j.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.sabicare_j.R
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.ui.auth.LoginActivity
import com.example.sabicare_j.ui.components.IconBadge
import com.example.sabicare_j.ui.components.SectionHeader
import com.example.sabicare_j.ui.components.TagPill
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    childViewModel: ChildViewModel,
    onAddChild: () -> Unit,
    onEditChild: (Long) -> Unit,
    onSettings: () -> Unit
) {
    val children by childViewModel.allChildren.observeAsState(emptyList())
    val activeChild by childViewModel.activeChild.observeAsState()
    val context = LocalContext.current

    var pendingDelete by remember { mutableStateOf<ChildEntity?>(null) }
    var showSignOut by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddChild,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_child)) },
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
            // Header gradient with email
            item { ProfileHeader() }

            // Active child big card
            item {
                Box(modifier = Modifier.padding(20.dp)) {
                    ActiveChildHero(activeChild)
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.my_children),
                    subtitle = stringResource(R.string.children_count_tap, children.size)
                )
            }
            items(children) { child ->
                ChildRow(
                    child = child,
                    isActive = child.id == activeChild?.id,
                    onSelect = { childViewModel.switchActiveChild(child.id) },
                    onEdit = { onEditChild(child.id) },
                    onDelete = { pendingDelete = child }
                )
            }
            if (children.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ChildCare,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.no_children_yet),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader(title = stringResource(R.string.account)) }
            item {
                MenuItem(
                    icon = Icons.Filled.Settings,
                    title = stringResource(R.string.settings),
                    subtitle = stringResource(R.string.settings_subtitle),
                    onClick = onSettings
                )
            }
            item {
                MenuItem(
                    icon = Icons.Filled.Logout,
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.sign_out_subtitle),
                    tintError = true,
                    onClick = { showSignOut = true }
                )
            }
        }
    }

    pendingDelete?.let { child ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_child_title)) },
            text = { Text(stringResource(R.string.confirm_delete_child_msg, child.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        childViewModel.deleteChild(child)
                        pendingDelete = null
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showSignOut) {
        AlertDialog(
            onDismissRequest = { showSignOut = false },
            title = { Text(stringResource(R.string.confirm_sign_out_title)) },
            text = { Text(stringResource(R.string.confirm_sign_out_msg)) },
            confirmButton = {
                Button(onClick = {
                    showSignOut = false
                    val activity = context as? androidx.activity.ComponentActivity
                    activity?.lifecycleScope?.launch {
                        runCatching {
                            (activity.application as SabiCareApplication).clearAllLocalData()
                        }
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(activity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }) { Text(stringResource(R.string.logout)) }
            },
            dismissButton = { TextButton(onClick = { showSignOut = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ProfileHeader() {
    val brush = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    )
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("sabicare_prefs_simple", android.content.Context.MODE_PRIVATE)
    }
    val displayName = user?.displayName ?: prefs.getString("user_display_name", stringResource(R.string.user_default))
    val email = user?.email ?: prefs.getString("user_email", "—")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush)
            .padding(top = 28.dp, bottom = 36.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    displayName ?: stringResource(R.string.user_default),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    email ?: "—",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ActiveChildHero(child: ChildEntity?) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (child == null) {
                Text(
                    stringResource(R.string.active_child_none),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.active_child_none_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = cs.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    if (!child.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = child.photoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (child.gender == "MALE") "👦" else "👧", fontSize = 32.sp)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        child.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row {
                        TagPill(text = ageString(child.birthDate))
                        Spacer(Modifier.width(6.dp))
                        TagPill(
                            text = if (child.gender == "MALE") stringResource(R.string.boy) else stringResource(R.string.girl),
                            background = cs.secondaryContainer,
                            contentColor = cs.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildRow(
    child: ChildEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) cs.primaryContainer else cs.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isActive) cs.primary else cs.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                if (!child.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = child.photoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (child.gender == "MALE") "👦" else "👧", fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    child.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) cs.onPrimaryContainer else cs.onSurface
                )
                Text(
                    ageString(child.birthDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) cs.onPrimaryContainer.copy(alpha = 0.7f) else cs.onSurfaceVariant
                )
            }
            if (isActive) {
                TagPill(text = stringResource(R.string.active))
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit), tint = cs.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = cs.error)
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            IconBadge(background = if (tintError) cs.errorContainer else cs.primaryContainer) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ageString(birthMillis: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - birthMillis).toInt()
    return when {
        days < 7 -> stringResource(R.string.child_age_days, days)
        days < 30 -> stringResource(R.string.child_age_weeks, days / 7)
        else -> stringResource(R.string.child_age_months, days / 30)
    }
}