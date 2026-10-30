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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.julietkioko.mserviceai.domain.model.ServiceCategory
import com.julietkioko.mserviceai.presentation.assistant.AssistantScreen
import com.julietkioko.mserviceai.presentation.details.DetailsScreen
import com.julietkioko.mserviceai.presentation.home.HomeScreen
import com.julietkioko.mserviceai.presentation.search.SearchScreen
import com.julietkioko.mserviceai.presentation.services.ServicesScreen

/**
 * Root composable that sets up the app's navigation structure.
 *
 * Architecture decisions:
 * - Uses a single [NavHost] inside a [Scaffold] with a bottom [NavigationBar].
 * - Each bottom-nav tap uses `launchSingleTop = true` and `popUpTo(startDestination)`
 *   to avoid stacking duplicate screens on the back stack.
 * - Centralizes navigation routing and arguments handling across screens.
 */
@Composable
fun MServiceApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on detail screens (they have their own back nav)
    val showBottomBar = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any {
            if (screen == Screen.Services) {
                it.route?.startsWith(Screen.Services.BASE_ROUTE) == true
            } else {
                it.route == screen.route
            }
        } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar || currentDestination == null) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            if (screen == Screen.Services) {
                                it.route?.startsWith(Screen.Services.BASE_ROUTE) == true
                            } else {
                                it.route == screen.route
                            }
                        } == true

                        val destinationRoute = if (screen == Screen.Services) {
                            Screen.Services.createRoute()
                        } else {
                            screen.route
                        }

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
                                navController.navigate(destinationRoute) {
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
                    onCategoryClick = { category ->
                        navController.navigate(Screen.Services.createRoute(category)) {
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

            // ── Services (Phase 2) ──────────────────────────────
            composable(
                route = Screen.Services.route,
                arguments = listOf(
                    navArgument(Screen.Services.ARG_CATEGORY) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val categoryParam = backStackEntry.arguments?.getString(Screen.Services.ARG_CATEGORY)
                val initialCategory = ServiceCategory.fromNameOrNull(categoryParam)

                ServicesScreen(
                    initialCategory = initialCategory,
                    onServiceClick = { serviceId ->
                        navController.navigate(Screen.Details.createRoute(serviceId))
                    }
                )
            }


            composable(Screen.Search.route) {
                SearchScreen(
                    onServiceClick = { serviceId ->
                        navController.navigate(Screen.Details.createRoute(serviceId))
                    }
                )
            }

            // ── Assistant (Phase 4) ─────────────────────────────
            composable(Screen.Assistant.route) {
                AssistantScreen(
                    onServiceClick = { serviceId ->
                        navController.navigate(Screen.Details.createRoute(serviceId))
                    }
                )
            }

            // ── Details (Phase 3) ───────────────────────────────
            composable(
                route = Screen.Details.route,
                arguments = listOf(
                    navArgument(Screen.Details.ARG_SERVICE_ID) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString(Screen.Details.ARG_SERVICE_ID) ?: ""
                DetailsScreen(
                    serviceId = serviceId,
                    onBackClick = {
                        navController.popBackStack()
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
