package com.example.sabicare_j.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sabicare_j.R
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sabicare_j.ui.screens.AddChildScreen
import com.example.sabicare_j.ui.screens.AddEntryScreen
import com.example.sabicare_j.ui.screens.HomeScreen
import com.example.sabicare_j.ui.screens.ProfileScreen
import com.example.sabicare_j.ui.screens.ResultsScreen
import com.example.sabicare_j.ui.screens.SettingsScreen
import com.example.sabicare_j.ui.screens.TrackerScreen
import com.example.sabicare_j.ui.shared.ChildViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SabiCareApp(childViewModel: ChildViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")
    val showBottomBar = currentRoute in Routes.bottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BottomBar(currentRoute = currentRoute) { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    childViewModel = childViewModel,
                    onAddChildClick = { navController.navigate(Routes.addChild()) },
                    onTrackerClick = { navController.navigate(Routes.TRACKER) },
                    onAddEntryClick = { type -> navController.navigate(Routes.addEntry(type)) }
                )
            }
            composable(Routes.TRACKER) {
                TrackerScreen(
                    childViewModel = childViewModel,
                    onAddEntry = { type -> navController.navigate(Routes.addEntry(type)) }
                )
            }
            composable(Routes.RESULTS) {
                ResultsScreen(childViewModel = childViewModel)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    childViewModel = childViewModel,
                    onAddChild = { navController.navigate(Routes.addChild()) },
                    onEditChild = { id -> navController.navigate(Routes.addChild(id)) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(
                Routes.ADD_ENTRY,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType; defaultValue = ""; nullable = true }
                )
            ) { entry ->
                val type = entry.arguments?.getString("type").orEmpty()
                AddEntryScreen(
                    childViewModel = childViewModel,
                    initialType = type.ifBlank { null },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Routes.ADD_CHILD,
                arguments = listOf(
                    navArgument("childId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val id = entry.arguments?.getLong("childId") ?: -1L
                AddChildScreen(
                    childViewModel = childViewModel,
                    editChildId = if (id > 0) id else null,
                    isOnboarding = false,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun BottomBar(currentRoute: String?, onClick: (String) -> Unit) {
    val items = listOf(
        NavItem(Routes.HOME, stringResource(R.string.nav_home), Icons.Filled.Home),
        NavItem(Routes.TRACKER, stringResource(R.string.nav_tracker), Icons.Filled.Timeline),
        NavItem(Routes.RESULTS, stringResource(R.string.nav_results), Icons.Filled.PieChart),
        NavItem(Routes.PROFILE, stringResource(R.string.nav_profile), Icons.Filled.Person)
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
