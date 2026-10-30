package com.ggs.qosqoruta.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggs.qosqoruta.core.di.AuthViewModelFactory
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.core.preferences.ThemeManager
import com.ggs.qosqoruta.features.auth.presentation.login.LoginScreen
import com.ggs.qosqoruta.features.auth.presentation.login.LoginViewModel
import com.ggs.qosqoruta.features.auth.presentation.register.RegisterScreen
import com.ggs.qosqoruta.features.auth.presentation.register.RegisterViewModel
import com.ggs.qosqoruta.features.mapa.presentation.HomeScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    themeManager: ThemeManager,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    val startDestination = if (sessionManager.isLoggedIn()) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            val context = LocalContext.current
            val factory = remember { AuthViewModelFactory(context) }
            val viewModel: LoginViewModel = viewModel(factory = factory)
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("splash") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            val context = LocalContext.current
            val factory = remember { AuthViewModelFactory(context) }
            val viewModel: RegisterViewModel = viewModel(factory = factory)
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("splash") {
            LaunchedEffect(Unit) {
                delay(2000)
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBus,
                            contentDescription = "Logo QosQoRuta",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "¡Bienvenido, ${sessionManager.getUserName()}!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        composable("main") {
            HomeScreen(
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onThemeChanged = { isDark -> themeManager.setDarkMode(isDark) },
                sessionManager = sessionManager
            )
        }
    }
}
