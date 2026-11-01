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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.julietkioko.mserviceai.data.local.AuthPreferencesDataStore
import com.julietkioko.mserviceai.data.repository.FirebaseAuthRepository
import com.julietkioko.mserviceai.domain.model.ServiceCategory
import com.julietkioko.mserviceai.domain.repository.AuthRepository
import com.julietkioko.mserviceai.presentation.assistant.AssistantScreen
import com.julietkioko.mserviceai.presentation.auth.SignInScreen
import com.julietkioko.mserviceai.presentation.auth.SignInViewModel
import com.julietkioko.mserviceai.presentation.auth.SignUpScreen
import com.julietkioko.mserviceai.presentation.auth.SignUpViewModel
import com.julietkioko.mserviceai.presentation.details.DetailsScreen
import com.julietkioko.mserviceai.presentation.home.HomeScreen
import com.julietkioko.mserviceai.presentation.search.SearchScreen
import com.julietkioko.mserviceai.presentation.services.ServicesScreen
import kotlinx.coroutines.launch

/**
 * Root composable that sets up the app's navigation structure.
 *
 * Architecture decisions:
 * - Local-first session state with DataStore + Firebase Authentication.
 * - Dynamic start destination: returning authenticated users go directly to [HomeScreen];
 *   unauthenticated users are presented with [SignInScreen].
 * - Bottom navigation is hidden on authentication and detail routes.
 * - Injects [AuthRepository] into [SignInViewModel] and [SignUpViewModel] via factory pattern.
 */
@Composable
fun MServiceApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    // Auth infrastructure dependencies
    val dataStore = remember { AuthPreferencesDataStore(context) }
    val authRepository: AuthRepository = remember {
        FirebaseAuthRepository(dataStore = dataStore)
    }

    // Initial check: if Firebase Auth has a cached user or DataStore has session
    val isInitiallyLoggedIn = remember {
        FirebaseAuth.getInstance().currentUser != null
    }

    val cachedUserName by dataStore.userName.collectAsState(initial = "")
    val resolvedUserName = cachedUserName.ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName ?: ""
    }

    // Determine whether to display the bottom bar (hidden on auth and details)
    val isAuthScreen = currentDestination?.route == Screen.SignIn.route ||
            currentDestination?.route == Screen.SignUp.route

    val showBottomBar = !isAuthScreen && Screen.bottomNavItems.any { screen ->
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
            if (showBottomBar) {
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
            startDestination = if (isInitiallyLoggedIn) Screen.Home.route else Screen.SignIn.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Authentication: Sign In ────────────────────────
            composable(Screen.SignIn.route) {
                val signInViewModel: SignInViewModel = viewModel(
                    factory = SignInViewModel.provideFactory(authRepository)
                )
                SignInScreen(
                    viewModel = signInViewModel,
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            // ── Authentication: Sign Up ────────────────────────
            composable(Screen.SignUp.route) {
                val signUpViewModel: SignUpViewModel = viewModel(
                    factory = SignUpViewModel.provideFactory(authRepository)
                )
                SignUpScreen(
                    viewModel = signUpViewModel,
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Home ────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    userName = resolvedUserName,
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
                    },
                    onSignOutClick = {
                        coroutineScope.launch {
                            authRepository.signOut()
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
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

            // ── Search ──────────────────────────────────────────
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
