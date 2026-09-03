package app.kumo.beta.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.kumo.beta.data.DemoData
import app.kumo.beta.data.local.PreferencesManager
import app.kumo.beta.ui.screens.details.DetailsScreen
import app.kumo.beta.ui.screens.home.HomeScreen
import app.kumo.beta.ui.screens.library.LibraryScreen
import app.kumo.beta.ui.screens.search.SearchScreen
import app.kumo.beta.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Library : Screen("library", "Library", Icons.Default.VideoLibrary)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Details : Screen("details/{titleId}", "Details", Icons.Default.Home) {
        fun create(titleId: String) = "details/$titleId"
    }
}

val bottomScreens = listOf(Screen.Home, Screen.Search, Screen.Library, Screen.Settings)

@Composable
fun KumoNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }
    var currentAccentColor by remember { mutableStateOf(prefs.accentColor) }
    var customHexColor by remember { mutableStateOf(prefs.customHexColor) }
    var useCustomHex by remember { mutableStateOf(prefs.useCustomHex) }

    val showBottomBar = bottomScreens.any { it.route == currentRoute }

    app.kumo.beta.ui.theme.KumoTheme(
        themeMode = currentThemeMode,
        accentOption = currentAccentColor,
        customHex = customHexColor,
        useCustomHex = useCustomHex
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        bottomScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label
                                    )
                                },
                                label = { Text(screen.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToDetails = { titleId ->
                            navController.navigate(Screen.Details.create(titleId))
                        },
                        onNavigateToSearch = {
                            navController.navigate(Screen.Search.route)
                        },
                        onNavigateToSearchWithFilter = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        onNavigateToDetails = { titleId ->
                            navController.navigate(Screen.Details.create(titleId))
                        }
                    )
                }
                composable(Screen.Library.route) {
                    LibraryScreen(
                        onNavigateToDetails = { titleId ->
                            navController.navigate(Screen.Details.create(titleId))
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onThemeChanged = { newTheme, newAccent, newCustomHex, newUseCustom ->
                            currentThemeMode = newTheme
                            currentAccentColor = newAccent
                            customHexColor = newCustomHex ?: "#7C4DFF"
                            useCustomHex = newUseCustom
                        }
                    )
                }
                composable(
                    route = Screen.Details.route,
                    arguments = listOf(navArgument("titleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val titleId = backStackEntry.arguments?.getString("titleId") ?: return@composable
                    val title = DemoData.getById(titleId)
                    if (title != null) {
                        DetailsScreen(
                            title = title,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
