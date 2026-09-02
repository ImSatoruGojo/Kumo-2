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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.kumo.beta.data.DemoData
import app.kumo.beta.ui.screens.details.DetailsScreen
import app.kumo.beta.ui.screens.home.HomeScreen
import app.kumo.beta.ui.screens.library.LibraryScreen
import app.kumo.beta.ui.screens.search.SearchScreen
import app.kumo.beta.ui.screens.settings.SettingsScreen
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoPurple
import app.kumo.beta.ui.theme.KumoSurface
import app.kumo.beta.ui.theme.KumoTextSecondary

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

    val showBottomBar = bottomScreens.any { it.route == currentRoute }

    Scaffold(
        containerColor = KumoBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = KumoSurface,
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
                                selectedIconColor = KumoPurple,
                                selectedTextColor = KumoPurple,
                                unselectedIconColor = KumoTextSecondary,
                                unselectedTextColor = KumoTextSecondary,
                                indicatorColor = KumoPurple.copy(alpha = 0.15f)
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
                    onTitleClick = { title ->
                        navController.navigate(Screen.Details.create(title.id))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onTitleClick = { title ->
                        navController.navigate(Screen.Details.create(title.id))
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
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
