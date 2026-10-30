package com.ggs.qosqoruta.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    themeManager: ThemeManager,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    
    // Si la sesión está activa, arranca directamente en la pantalla principal ("main")
    val startDestination = if (sessionManager.isLoggedIn()) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        
        // PANTALLA DE LOGIN
        composable("login") {
            val context = LocalContext.current
            val factory = remember { AuthViewModelFactory(context) }
            val viewModel: LoginViewModel = viewModel(factory = factory)
            
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    // Navegamos al main limpiando la pila para que no pueda volver atrás con back
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA DE REGISTRO
        composable("register") {
            val context = LocalContext.current
            val factory = remember { AuthViewModelFactory(context) }
            val viewModel: RegisterViewModel = viewModel(factory = factory)
            
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack() // Vuelve hacia el login
                }
            )
        }

        // PANTALLA PRINCIPAL (MAPA, RUTAS, ETC)
        composable("main") {
            HomeScreen(
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onThemeChanged = { isDark -> themeManager.setDarkMode(isDark) }
            )
        }
    }
}
