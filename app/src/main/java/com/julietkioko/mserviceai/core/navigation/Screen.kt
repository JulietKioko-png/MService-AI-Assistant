package com.julietkioko.mserviceai.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.julietkioko.mserviceai.domain.model.ServiceCategory

/**
 * Type-safe navigation destinations for the app.
 *
 * Using a sealed class ensures the Kotlin compiler can verify that every
 * screen is handled in `when` expressions (exhaustive matching).
 * This prevents navigation bugs that string-based routes can introduce.
 *
 * [route] is the string key used by Compose Navigation's [androidx.navigation.compose.NavHost].
 * [title] is the human-readable label for app bars and bottom nav.
 * [icon]  is the Material icon for bottom navigation items.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)

    data object Services : Screen("services?category={category}", "Services", Icons.AutoMirrored.Filled.List) {
        const val BASE_ROUTE = "services"
        const val ARG_CATEGORY = "category"

        /** Build route with optional category filter. */
        fun createRoute(category: ServiceCategory? = null): String {
            return if (category != null) "$BASE_ROUTE?$ARG_CATEGORY=${category.name}" else BASE_ROUTE
        }
    }

    data object Search : Screen("search", "Search", Icons.Filled.Search)

    data object Assistant : Screen("assistant", "Assistant", Icons.Filled.Info)

    /** Detail screen — not shown in bottom nav; navigated to from other screens. */
    data object Details : Screen("details/{serviceId}", "Details", Icons.Filled.Info) {
        const val ARG_SERVICE_ID = "serviceId"

        /** Build the concrete route for a specific service. */
        fun createRoute(serviceId: String): String = "details/$serviceId"
    }

    /** Authentication screen for existing user login. */
    data object SignIn : Screen("sign_in", "Sign In", Icons.Filled.Lock)

    /** Authentication screen for new user registration. */
    data object SignUp : Screen("sign_up", "Sign Up", Icons.Filled.Lock)

    companion object {
        /** Screens displayed as tabs in the bottom navigation bar. */
        val bottomNavItems = listOf(Home, Services, Search, Assistant)
    }
}
