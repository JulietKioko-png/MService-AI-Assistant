package com.julietkioko.mserviceai.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.julietkioko.mserviceai.presentation.home.HomeScreen

/**
 * Root composable that sets up the app's navigation structure.
 *
 * Architecture decisions:
 * - Uses a single [NavHost] inside a [Scaffold] with a bottom
 *   [NavigationBar]. This is the standard pattern for tab-based Android
 *   apps — the scaffold stays fixed while screen content swaps.
 * - The [NavController] lives here (via [rememberNavController]) so it
 *   survives configuration changes (rotation, dark mode toggle).
 * - Each bottom-nav tap uses `launchSingleTop = true` and
 *   `popUpTo(startDestination)` to avoid stacking duplicate screens
 *   on the back stack — this matches the behaviour users expect from
 *   bottom-navigation apps.
 *
 * Production value: Centralising navigation here means adding a new
 * screen is a two-step process: (1) add a route to [Screen],
 * (2) add a `composable(...)` block below. No other files change.
 */
@Composable
fun MServiceApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on detail screens (they have their own back nav)
    val showBottomBar = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar || currentDestination == null) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to start destination to avoid building
                                    // a large back stack when switching tabs
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Home ────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onCategoryClick = { categoryName ->
                        // Phase 2: navigate to Services with category filter
                        navController.navigate(Screen.Services.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAskAssistantClick = {
                        navController.navigate(Screen.Assistant.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ── Services (placeholder — Phase 2) ────────────────
            composable(Screen.Services.route) {
                PlaceholderScreen(title = "Services", subtitle = "Browse government services")
            }

            // ── Search (placeholder — Phase 3) ──────────────────
            composable(Screen.Search.route) {
                PlaceholderScreen(title = "Search", subtitle = "Find any service instantly")
            }

            // ── Assistant (placeholder — Phase 4) ───────────────
            composable(Screen.Assistant.route) {
                PlaceholderScreen(title = "AI Assistant", subtitle = "Ask me anything about\ngovernment services")
            }

            // ── Details (placeholder — Phase 3) ─────────────────
            composable(Screen.Details.route) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
                PlaceholderScreen(title = "Service Details", subtitle = "Details for: $serviceId")
            }
        }
    }
}

/**
 * Temporary placeholder for screens that will be built in later phases.
 * Replaced screen-by-screen as each phase is completed.
 */
@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title\n$subtitle",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
