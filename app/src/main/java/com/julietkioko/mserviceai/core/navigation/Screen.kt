package com.julietkioko.mserviceai.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation destinations for the app.
 *
 * Using a sealed class ensures the Kotlin compiler can verify that every
 * screen is handled in `when` expressions (exhaustive matching).
 * This prevents navigation bugs that string-based routes can introduce.
 *
 * [route] is the string key used by Compose Navigation's [NavHost].
 * [title] is the human-readable label for app bars and bottom nav.
 * [icon]  is the Material icon for bottom navigation items.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Services : Screen("services", "Services", Icons.AutoMirrored.Filled.List)
    data object Search : Screen("search", "Search", Icons.Filled.Search)
    data object Assistant : Screen("assistant", "Assistant", Icons.Filled.Info)

    /** Detail screen — not shown in bottom nav; navigated to from other screens. */
    data object Details : Screen("details/{serviceId}", "Details", Icons.Filled.Info) {
        /** Build the concrete route for a specific service. */
        fun createRoute(serviceId: String): String = "details/$serviceId"
    }

    companion object {
        /** Screens displayed as tabs in the bottom navigation bar. */
        val bottomNavItems = listOf(Home, Services, Search, Assistant)
    }
}
