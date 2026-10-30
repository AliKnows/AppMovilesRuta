package com.ggs.qosqoruta.features.mapa.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

// ---------------------------------------------------------
// RUTAS Y NAVEGACIÓN
// ---------------------------------------------------------
sealed class Rutas(val route: String) {
    object MapaPrincipal : Rutas("mapa_principal")
    object ModoPasajero : Rutas("modo_pasajero")
    object LineasEmpresas : Rutas("lineas_empresas")
    object Paraderos : Rutas("paraderos")
    object Tarifas : Rutas("tarifas")
    object Historial : Rutas("historial")
    object Offline : Rutas("offline")
    object Configuracion : Rutas("configuracion")
}

// ---------------------------------------------------------
// MODELO DE DATOS Y LISTA DE BUSES (SIMULADA)
// ---------------------------------------------------------
data class BusInfo(
    val id: String,
    val name: String,
    val tag: String,
    val route: String,
    val eta: String,
    val distance: String,
    val occupancy: String,
    val occupancyColor: Color,
    val price: String,
    val frequency: String,
    val statusText: String,
    val iconColor: Color
)

val availableBuses = listOf(
    BusInfo("B1", "Expreso Batman", "RT-04", "San Jerónimo ➔ UNSAAC ➔ San Pedro", "3 min", "A 350 metros", "Media (6 libres)", Color(0xFFD84315), "S/. 1.00", "Cada 4 min", "Asientos limitados", Color(0xFF1565C0)),
    BusInfo("B2", "Servicio C4M", "C4-M", "San Jerónimo ➔ Marcavalle ➔ Magisterio", "7 min", "A 1.2 km", "Alta (2 libres)", Color.Red, "S/. 1.50", "Cada 8 min", "Asientos libres disponibles", Color.Gray),
    BusInfo("B3", "Ttio La Florida", "R-21", "Oropesa ➔ Wanchaq ➔ Aeropuerto", "12 min", "A 2.5 km", "Baja (15 libres)", Color(0xFF388E3C), "S/. 1.00", "Cada 10 min", "Ruta despejada", Color.Gray)
)

// ---------------------------------------------------------
// HOMESCREEN ROOT (CONTENEDOR PRINCIPAL)
// ---------------------------------------------------------
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
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
                    Text(
                        text = "QosqoRuta",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pasajero Andino",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val drawerItems = listOf(
                        Triple("Mapa Principal", Icons.Filled.Map, Rutas.MapaPrincipal),
                        Triple("Modo Pasajero", Icons.Filled.Person, Rutas.ModoPasajero),
                        Triple("Líneas y Empresas de Cusco", Icons.Filled.DirectionsBus, Rutas.LineasEmpresas),
                        Triple("Paraderos Cercanos", Icons.Filled.PinDrop, Rutas.Paraderos),
                        Triple("Tarifas y Pasajes", Icons.Filled.AttachMoney, Rutas.Tarifas),
                        Triple("Historial de Rutas", Icons.Filled.History, Rutas.Historial),
                        Triple("Modo Desconectado (Offline)", Icons.Filled.SignalWifiOff, Rutas.Offline),
                        Triple("Configuración", Icons.Filled.Settings, Rutas.Configuracion)
                    )

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

                    // Botón estático de cerrar sesión en el menú
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
        // Enrutador Jetpack Navigation Compose
        NavHost(
            navController = navController,
            startDestination = Rutas.MapaPrincipal.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Rutas.MapaPrincipal.route) {
                MapaScreen(onOpenMenu = { scope.launch { drawerState.open() } })
            }
            composable(Rutas.Configuracion.route) {
                ConfiguracionScreen(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    onCerrarSesion = onLogout,
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged
                )
            }
            // Pantallas genéricas en construcción
            composable(Rutas.ModoPasajero.route) { PantallaEnConstruccion("Modo Pasajero", onOpenMenu = { scope.launch { drawerState.open() } }) }
            composable(Rutas.LineasEmpresas.route) { PantallaEnConstruccion("Líneas y Empresas", onOpenMenu = { scope.launch { drawerState.open() } }) }
            composable(Rutas.Paraderos.route) { PantallaEnConstruccion("Paraderos Cercanos", onOpenMenu = { scope.launch { drawerState.open() } }) }
            composable(Rutas.Tarifas.route) { PantallaEnConstruccion("Tarifas y Pasajes", onOpenMenu = { scope.launch { drawerState.open() } }) }
            composable(Rutas.Historial.route) { PantallaEnConstruccion("Historial de Rutas", onOpenMenu = { scope.launch { drawerState.open() } }) }
            composable(Rutas.Offline.route) { PantallaEnConstruccion("Modo Offline", onOpenMenu = { scope.launch { drawerState.open() } }) }
        }
    }
}

// ---------------------------------------------------------
// PANTALLAS (MÓDULOS DE NAVEGACIÓN)
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEnConstruccion(titulo: String, onOpenMenu: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) { Icon(Icons.Filled.Menu, contentDescription = "Menú") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "En construcción",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pantalla $titulo en construcción",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onOpenMenu: () -> Unit, 
    onCerrarSesion: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    var notificacionesPush by remember { mutableStateOf(true) }
    var showDriverDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var currentRole by remember { mutableStateOf(sessionManager.getUserRole()) }

    var companyInput by remember { mutableStateOf("") }
    var routeInput by remember { mutableStateOf("") }

    if (showDriverDialog) {
        AlertDialog(
            onDismissRequest = { showDriverDialog = false },
            title = { Text("Datos de Conductor") },
            text = {
                Column {
                    Text("Para cambiar a Conductor, ingresa tu empresa y ruta de hoy.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = companyInput,
                        onValueChange = { companyInput = it },
                        label = { Text("Empresa (Ej. Nuevo Mirador)") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = routeInput,
                        onValueChange = { routeInput = it },
                        label = { Text("Ruta (Ej. RTI-07)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (companyInput.isNotBlank() && routeInput.isNotBlank()) {
                        sessionManager.updateRoleToDriver(companyInput, routeInput)
                        currentRole = UserRole.CONDUCTOR
                        showDriverDialog = false
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDriverDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) { Icon(Icons.Filled.Menu, contentDescription = "Menú") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Sección Cuenta
            Text("Cuenta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(sessionManager.getUserName(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Surface(color = if (currentRole == UserRole.CONDUCTOR) Color(0xFF00695C) else Color(0xFF1565C0), shape = RoundedCornerShape(4.dp)) {
                        Text(currentRole.name, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // Sección Preferencias
            Text("Preferencias", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Notificaciones Push", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = notificacionesPush, onCheckedChange = { notificacionesPush = it })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Modo Oscuro", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDarkMode, onCheckedChange = onThemeChanged)
            }
            
            // Botón Cambio de Rol
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    if (currentRole == UserRole.PASAJERO) {
                        showDriverDialog = true
                    } else {
                        sessionManager.updateRoleToPassenger()
                        currentRole = UserRole.PASAJERO
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (currentRole == UserRole.PASAJERO) Icons.Filled.DirectionsCar else Icons.Filled.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentRole == UserRole.PASAJERO) "Cambiar a Modo Conductor" else "Volver a Modo Pasajero")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // Sección Acerca de
            Text("Acerca de", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Versión de la aplicación: 1.0.0 (Alpha)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.weight(1f))

            // Botón de Cerrar Sesión
            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(onOpenMenu: () -> Unit) {
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0 = Explorar, 2 = En Vivo
    var activeBus by remember { mutableStateOf(availableBuses[0]) }

    val cuscoLatLng = LatLng(-13.5226, -71.9673)
    val busLatLng = LatLng(-13.5280, -71.9600)
    val userLatLng = LatLng(-13.5240, -71.9650)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cuscoLatLng, 15f)
    }

    LaunchedEffect(selectedBottomTab) {
        if (selectedBottomTab == 2) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 16f)
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(cuscoLatLng, 15f)
        }
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Filled.DirectionsBus, contentDescription = "Explorar") },
                    label = { Text("Explorar", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1565C0),
                        selectedTextColor = Color(0xFF1565C0),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Rutas") },
                    label = { Text("Rutas", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1565C0),
                        selectedTextColor = Color(0xFF1565C0),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.Filled.Navigation, contentDescription = "En Vivo") },
                    label = { Text("En Vivo", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1565C0),
                        selectedTextColor = Color(0xFF1565C0),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Favoritos") },
                    label = { Text("Favoritos", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1565C0),
                        selectedTextColor = Color(0xFF1565C0),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
            }
        }
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = if (selectedBottomTab == 0 || selectedBottomTab == 2) 420.dp else 100.dp,
            sheetContainerColor = Color.White,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContent = {
                when (selectedBottomTab) {
                    0 -> ExplorarBottomSheetContent(
                        activeBus = activeBus,
                        onBusSelected = { activeBus = it },
                        onFollowBusClick = { selectedBottomTab = 2 }
                    )
                    2 -> EnVivoBottomSheetContent(
                        activeBus = activeBus,
                        onStopFollowingClick = { selectedBottomTab = 0 }
                    )
                    else -> Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Sección en construcción", color = Color.Gray)
                    }
                }
            }
        ) { sheetPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(sheetPadding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)
                ) {
                    if (selectedBottomTab == 2) {
                        Polyline(points = listOf(busLatLng, userLatLng), color = Color(0xFF1565C0), width = 12f)
                        Marker(state = MarkerState(position = busLatLng), title = activeBus.name, snippet = "En movimiento")
                        Marker(state = MarkerState(position = userLatLng), title = "Paradero Manuel Prado", snippet = "Tu ubicación")
                    }
                }

                FilledIconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1565C0)
                    )
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menú")
                }
            }
        }
    }
}

// ---------------------------------------------------------
// COMPONENTE DE TARJETA DESPLEGABLE
// ---------------------------------------------------------
@Composable
fun ExpandableBusCard(
    bus: BusInfo,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color.White else Color(0xFFF5F7FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp),
        shape = RoundedCornerShape(if (isExpanded) 24.dp else 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CABECERA SIEMPRE VISIBLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isExpanded) Color(0xFF1565C0) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBus,
                            contentDescription = null,
                            tint = if (isExpanded) Color.White else Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bus.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isExpanded) Color(0xFFE3F2FD) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = bus.tag,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isExpanded) Color(0xFF1565C0) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = bus.route,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.wrapContentWidth()) {
                    Surface(
                        color = if (isExpanded) Color(0xFFFF6D00) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isExpanded) {
                                Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = bus.eta,
                                color = if (isExpanded) Color.White else Color.DarkGray,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = bus.distance, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                }
            }

            // CONTENIDO DESPLEGABLE
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Estadísticas dinámicas adaptables
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Ocupación", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = bus.occupancyColor, modifier = Modifier.size(16.dp))
                                Text(bus.occupancy, style = MaterialTheme.typography.labelSmall, color = bus.occupancyColor, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Pasaje Exacto", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                            Text(bus.price, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Frecuencia", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                            Text(bus.frequency, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00695C), maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onFollowClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.NearMe, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seguir este bus en vivo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedIconButton(
                            onClick = { /* Guardar favorito */ },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.BookmarkBorder, contentDescription = "Guardar", tint = Color.Gray)
                        }
                    }
                }
            }

            // CONTENIDO CONTRAÍDO
            AnimatedVisibility(visible = !isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.CheckCircleOutline, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(bus.statusText, style = MaterialTheme.typography.bodySmall, color = Color(0xFF388E3C), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("Ver ruta ➔", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PANTALLA EXPLORAR (0)
// ---------------------------------------------------------
@Composable
fun ExplorarBottomSheetContent(
    activeBus: BusInfo,
    onBusSelected: (BusInfo) -> Unit,
    onFollowBusClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera: Ubicación y Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Paradero Manuel Prado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Hacia Wanchaq • Centro • UNSAAC", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = Color(0xFFE0F2F1), shape = RoundedCornerShape(16.dp), modifier = Modifier.wrapContentWidth()) {
                Text("● MODO PASAJERO", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF00695C), fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // GENERACIÓN DINÁMICA DE TARJETAS TIPO ACORDEÓN
        availableBuses.forEach { bus ->
            ExpandableBusCard(
                bus = bus,
                isExpanded = bus.id == activeBus.id,
                onClick = { onBusSelected(bus) },
                onFollowClick = onFollowBusClick
            )
        }

        // TARJETA INFO
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Consejo al pasajero", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1565C0), maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paga con sencillo (S/. 1.00 o S/. 2.00) al subir por la puerta delantera.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ---------------------------------------------------------
// PANTALLA EN VIVO (2)
// ---------------------------------------------------------
@Composable
fun EnVivoBottomSheetContent(activeBus: BusInfo, onStopFollowingClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alerta en vivo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Transmisión En Vivo", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }

        // Bus info y ETA dinámica basada en el bus seleccionado
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1565C0),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(activeBus.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Unidad ${activeBus.tag}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(activeBus.eta, color = Color(0xFFFF6D00), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(activeBus.distance, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Línea de tiempo (Simulada)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Siguientes paradas:", style = MaterialTheme.typography.labelLarge, color = Color.DarkGray)
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Circle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("1. Paradero Garcilaso", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("Llega en 1 min", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("2. Paradero Manuel Prado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Tu parada actual", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Acción
        OutlinedButton(
            onClick = onStopFollowingClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Dejar de seguir")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
