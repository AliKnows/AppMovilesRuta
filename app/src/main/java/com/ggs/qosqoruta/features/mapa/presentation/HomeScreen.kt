package com.ggs.qosqoruta.features.mapa.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
import com.ggs.qosqoruta.features.mapa.presentation.components.PantallaEnConstruccion
import com.ggs.qosqoruta.features.mapa.presentation.components.ProfileAvatarImage
import com.ggs.qosqoruta.features.mapa.presentation.configuracion.ConfiguracionScreen
import com.ggs.qosqoruta.features.mapa.presentation.mapa.MapaScreen
import com.ggs.qosqoruta.features.mapa.presentation.navigation.Rutas
import com.ggs.qosqoruta.features.mapa.presentation.screens.HistorialRutasScreen
import com.ggs.qosqoruta.features.mapa.presentation.screens.LineasEmpresasScreen
import com.ggs.qosqoruta.features.mapa.presentation.screens.ModoOfflineScreen
import com.ggs.qosqoruta.features.mapa.presentation.screens.ParaderosCercanosScreen
import com.ggs.qosqoruta.features.mapa.presentation.screens.TarifasScreen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    sessionManager: SessionManager = SessionManager(LocalContext.current)
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                    ProfileAvatarImage(
                        avatarUri = sessionManager.getAvatarUri(),
                        avatarId = sessionManager.getAvatarId(),
                        modifier = Modifier.size(60.dp),
                        iconSize = 36.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = sessionManager.getFullName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (sessionManager.getUserRole() == UserRole.CONDUCTOR) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (sessionManager.getUserRole() == UserRole.CONDUCTOR) "CONDUCTOR • ${sessionManager.getDriverCompany()}" else "PASAJERO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (sessionManager.getUserRole() == UserRole.CONDUCTOR) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val isDriver = sessionManager.getUserRole() == UserRole.CONDUCTOR
                    val drawerItems = listOf(
                        Triple("Mapa Principal", Icons.Filled.Map, Rutas.MapaPrincipal),
                        Triple("Líneas y Empresas de Cusco", Icons.Filled.DirectionsBus, Rutas.LineasEmpresas),
                        Triple("Paraderos Cercanos", Icons.Filled.PinDrop, Rutas.Paraderos),
                        Triple("Tarifas y Pasajes", Icons.Filled.AttachMoney, Rutas.Tarifas),
                        Triple("Historial de Rutas", Icons.Filled.History, Rutas.Historial),
                        Triple("Modo Desconectado (Offline)", Icons.Filled.SignalWifiOff, Rutas.Offline),
                        Triple("Configuración", Icons.Filled.Settings, Rutas.Configuracion)
                    ).filter { (title, _, _) ->
                        !(isDriver && title == "Paraderos Cercanos")
                    }

                    drawerItems.forEach { (title, icon, route) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                        label = { Text("Cerrar Sesión", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Rutas.MapaPrincipal.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Rutas.MapaPrincipal.route) {
                MapaScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged
                )
            }
            composable(Rutas.Configuracion.route) {
                ConfiguracionScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    onCerrarSesion = onLogout,
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged,
                    onNavigateToMap = {
                        navController.navigate(Rutas.MapaPrincipal.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Rutas.LineasEmpresas.route) {
                LineasEmpresasScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged
                )
            }
            composable(Rutas.Tarifas.route) {
                TarifasScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged
                )
            }
            composable(Rutas.Paraderos.route) {
                ParaderosCercanosScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged
                )
            }
            composable(Rutas.Historial.route) {
                HistorialRutasScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged,
                    onNavigateToMap = {
                        navController.navigate(Rutas.MapaPrincipal.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Rutas.Offline.route) {
                ModoOfflineScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged,
                    onNavigateToLineas = { navController.navigate(Rutas.LineasEmpresas.route) },
                    onNavigateToParaderos = { navController.navigate(Rutas.Paraderos.route) },
                    onNavigateToTarifas = { navController.navigate(Rutas.Tarifas.route) },
                    onNavigateToMap = {
                        navController.navigate(Rutas.MapaPrincipal.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Rutas.ModoPasajero.route) { PantallaEnConstruccion("Modo Pasajero", onOpenMenu = { scope.launch { drawerState.open() } }, isDarkMode = isDarkMode, onThemeChanged = onThemeChanged) }
        }
    }
}
