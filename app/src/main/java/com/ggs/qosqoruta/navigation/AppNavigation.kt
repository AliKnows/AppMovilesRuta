package com.ggs.qosqoruta.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggs.qosqoruta.R
import com.ggs.qosqoruta.core.di.AuthViewModelFactory
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.core.preferences.ThemeManager
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
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
            val scale = remember { Animatable(0.4f) }
            val userRole = sessionManager.getUserRole()
            val isDriver = userRole == UserRole.CONDUCTOR

            LaunchedEffect(Unit) {
                scale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                delay(1200)
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
                        if (isDriver) {
                            // MODO CONDUCTOR: LOGO DE AUTOBÚS QOSQORUTA
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_qosqoruta),
                                contentDescription = "Logo Conductor",
                                modifier = Modifier
                                    .size(130.dp)
                                    .graphicsLayer(
                                        scaleX = scale.value,
                                        scaleY = scale.value
                                    )
                            )
                        } else {
                            // MODO PASAJERO: PERSONA CAMINANDO
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(130.dp)
                                    .graphicsLayer(
                                        scaleX = scale.value,
                                        scaleY = scale.value
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                        contentDescription = "Pasajero Caminando",
                                        modifier = Modifier.size(72.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "¡Bienvenido, ${sessionManager.getUserName()}!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (isDriver) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isDriver) "CONDUCTOR • ${sessionManager.getDriverCompany()}" else "MODO PASAJERO",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDriver) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
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
