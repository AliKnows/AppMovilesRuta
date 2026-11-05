package com.ggs.qosqoruta.features.mapa.presentation.mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
import com.ggs.qosqoruta.features.mapa.data.*
import com.ggs.qosqoruta.features.mapa.presentation.components.EnVivoBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.ExplorarBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.FavoritosBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.RutasBottomSheetContent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// GENERADOR DEL ÍCONO PERSONALIZADO DE AUTOBÚS PROTEGIDO CONTRA ERRORES DE INICIALIZACIÓN DE MAPAS
fun getCustomBusIconDescriptor(context: Context): BitmapDescriptor {
    return try {
        MapsInitializer.initialize(context)
        val width = 110
        val height = 70
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 7f
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val bodyRect = RectF(8f, 8f, 102f, 54f)
        canvas.drawRoundRect(bodyRect, 16f, 16f, fillPaint)
        canvas.drawRoundRect(bodyRect, 16f, 16f, strokePaint)

        val windowRect = RectF(16f, 16f, 94f, 32f)
        canvas.drawRoundRect(windowRect, 6f, 6f, strokePaint)

        val wheelPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val innerWheelPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        canvas.drawCircle(28f, 54f, 10f, wheelPaint)
        canvas.drawCircle(28f, 54f, 4f, innerWheelPaint)

        canvas.drawCircle(82f, 54f, 10f, wheelPaint)
        canvas.drawCircle(82f, 54f, 4f, innerWheelPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Throwable) {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onOpenMenu: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onRouteSelected: (origen: String, destino: String, linea: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val busIconDescriptor = remember(context) {
        try {
            getCustomBusIconDescriptor(context)
        } catch (e: Throwable) {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
        }
    }

    val userRole = sessionManager.getUserRole()
    val isDriver = userRole == UserRole.CONDUCTOR
    val company = sessionManager.getDriverCompany()
    val route = sessionManager.getDriverRoute()

    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var selectedRouteTag by remember { mutableStateOf("RTI-07") }
    var mapSearchQuery by remember { mutableStateOf("") }

    var userLatLng by remember {
        mutableStateOf(LatLng(-13.5240, -71.9650))
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) userLatLng = LatLng(loc.latitude, loc.longitude)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) userLatLng = LatLng(loc.latitude, loc.longitude)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        } else {
            try {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    val initialFavs = remember { sessionManager.getFavoriteBusIds() }
    val busesList = remember {
        mutableStateListOf(
            *availableBuses.map { bus ->
                bus.copy(isFavorite = initialFavs.contains(bus.id))
            }.toTypedArray()
        )
    }
    var activeBus by remember { mutableStateOf(busesList[0]) }

    var blueLineProgress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(selectedBottomTab, activeBus.id) {
        if (selectedBottomTab == 2) {
            for (repeatCount in 1..3) {
                for (step in 1..10) {
                    blueLineProgress = step / 10f
                    delay(60)
                }
                if (repeatCount < 3) {
                    delay(150)
                    blueLineProgress = 0.1f
                }
            }
            blueLineProgress = 1f
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 14f)
    }

    LaunchedEffect(selectedBottomTab) {
        val currentFavs = sessionManager.getFavoriteBusIds()
        busesList.indices.forEach { index ->
            val bus = busesList[index]
            val isFav = currentFavs.contains(bus.id)
            if (bus.isFavorite != isFav) {
                busesList[index] = bus.copy(isFavorite = isFav)
            }
        }
    }

    val activeRoutePolyline = when (selectedRouteTag) {
        "RTI-07", "RT-04" -> rti07NuevoMiradorPolyline
        "RTU-01", "C4-M" -> rtu01PatronPolyline
        "RTI-01", "R-21" -> rti01SayllaHuasaoPolyline
        "LEONES", "LOS LEONES" -> losLeonesPolyline
        else -> rti07NuevoMiradorPolyline
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    val toggleFavorite: (String) -> Unit = { busId ->
        val updatedFavs = sessionManager.toggleFavoriteBus(busId)
        val index = busesList.indexOfFirst { it.id == busId }
        if (index != -1) {
            val bus = busesList[index]
            val updated = bus.copy(isFavorite = updatedFavs.contains(busId))
            busesList[index] = updated
            if (activeBus.id == busId) activeBus = updated
        }
    }

    val removeFavorite: (String) -> Unit = { busId ->
        val updatedFavs = sessionManager.removeFavoriteBus(busId)
        val index = busesList.indexOfFirst { it.id == busId }
        if (index != -1) {
            val bus = busesList[index]
            val updated = bus.copy(isFavorite = false)
            busesList[index] = updated
            if (activeBus.id == busId) activeBus = updated
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.Filled.DirectionsBus, contentDescription = "Explorar") },
                    label = { Text("Explorar", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = "Rutas") },
                    label = { Text("Rutas", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.Filled.Navigation, contentDescription = "En Vivo") },
                    label = { Text("En Vivo", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = {
                        selectedBottomTab = 3
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Favoritos") },
                    label = { Text("Favoritos", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = if (selectedBottomTab == 0 || selectedBottomTab == 1 || selectedBottomTab == 2) 200.dp else 100.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContent = {
                when (selectedBottomTab) {
                    0 -> ExplorarBottomSheetContent(
                        buses = busesList,
                        activeBus = activeBus,
                        isDriver = isDriver,
                        company = company,
                        route = route,
                        externalSearchQuery = mapSearchQuery,
                        onBusSelected = { bus ->
                            activeBus = bus
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(bus.latLng, 16f)
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onFollowBusClick = { bus ->
                            activeBus = bus
                            selectedBottomTab = 2
                            // GUARDA AUTOMÁTICAMENTE LA RUTA SELECCIONADA EN EL HISTORIAL
                            onRouteSelected("Plaza de Armas", "Paradero Final", bus.tag)
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onToggleFavorite = toggleFavorite,
                        onViewStopOnMap = { paradero ->
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(paradero.location, 16f)
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )
                    1 -> RutasBottomSheetContent(
                        selectedRouteTag = selectedRouteTag,
                        onSelectRoute = { routeCode ->
                            selectedRouteTag = routeCode
                            val busMatch = busesList.find { it.tag == routeCode }
                            if (busMatch != null) activeBus = busMatch

                            // GUARDA LA RUTA CONSULTADA EN LA BASE DE DATOS LOCAL (ROOM)
                            val (origen, destino) = when (routeCode) {
                                "RTI-07", "RT-04" -> "Nuevo Mirador" to "San Jerónimo"
                                "RTU-01", "C4-M" -> "Plaza de Armas" to "Patrón San Jerónimo"
                                "RTI-01", "R-21" -> "Saylla" to "Huasao"
                                else -> "Cusco Centro" to "Paradero Final"
                            }
                            onRouteSelected(origen, destino, routeCode)

                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )
                    2 -> EnVivoBottomSheetContent(
                        activeBus = activeBus,
                        onStopFollowingClick = {
                            selectedBottomTab = 0
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )
                    3 -> FavoritosBottomSheetContent(
                        favoriteBuses = busesList.filter { it.isFavorite },
                        onFollowBusClick = { bus ->
                            activeBus = bus
                            selectedRouteTag = bus.tag
                            selectedBottomTab = 2
                            onRouteSelected("Ubicación Actual", "Paradero Frecuente", bus.tag)
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onRemoveFavorite = removeFavorite
                    )
                }
            }
        ) { sheetPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(sheetPadding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)
                ) {
                    Marker(
                        state = MarkerState(position = userLatLng),
                        title = "📍 Tu Ubicación Actual",
                        snippet = "Estás aquí",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )

                    when (selectedBottomTab) {
                        0 -> {
                            cuscoParaderosReales.forEach { paradero ->
                                Marker(
                                    state = MarkerState(position = paradero.location),
                                    title = paradero.name,
                                    snippet = paradero.snippet,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )
                            }
                            busesList.forEach { bus ->
                                Marker(
                                    state = MarkerState(position = bus.latLng),
                                    title = "${bus.name} (${bus.tag}) - Cond: ${bus.driverName}",
                                    snippet = "Ocupación: ${bus.occupancy} | ETA: ${bus.eta}",
                                    icon = busIconDescriptor
                                )
                            }
                        }
                        1 -> {
                            Polyline(
                                points = activeRoutePolyline,
                                color = Color(0xFFE53935),
                                width = 14f
                            )

                            val routeParaderos = cuscoParaderosReales.filter { paradero ->
                                when (selectedRouteTag) {
                                    "RTI-07", "RT-04" -> paradero.snippet.contains("RTI-07")
                                    "RTU-01", "C4-M" -> paradero.snippet.contains("RTU-01")
                                    "RTI-01", "R-21" -> paradero.snippet.contains("RTI-01")
                                    "LEONES", "LOS LEONES" -> paradero.snippet.contains("Leones")
                                    else -> true
                                }
                            }
                            routeParaderos.forEach { paradero ->
                                Marker(
                                    state = MarkerState(position = paradero.location),
                                    title = paradero.name,
                                    snippet = paradero.snippet,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )
                            }

                            val routeBuses = busesList.filter { bus ->
                                when (selectedRouteTag) {
                                    "RTI-07", "RT-04" -> bus.tag == "RTI-07" || bus.tag == "RT-04"
                                    "RTU-01", "C4-M" -> bus.tag == "RTU-01" || bus.tag == "C4-M"
                                    "RTI-01", "R-21" -> bus.tag == "RTI-01" || bus.tag == "R-21"
                                    else -> true
                                }
                            }
                            routeBuses.forEach { bus ->
                                Marker(
                                    state = MarkerState(position = bus.latLng),
                                    title = "${bus.name} (${bus.tag}) - Cond: ${bus.driverName}",
                                    snippet = "Ocupación: ${bus.occupancy} | ETA: ${bus.eta}",
                                    icon = busIconDescriptor
                                )
                            }
                        }
                        2 -> {
                            val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 3) }
                            val arrivalTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

                            val currentEndLat = activeBus.latLng.latitude + (userLatLng.latitude - activeBus.latLng.latitude) * blueLineProgress
                            val currentEndLng = activeBus.latLng.longitude + (userLatLng.longitude - activeBus.latLng.longitude) * blueLineProgress
                            val currentEndLatLng = LatLng(currentEndLat, currentEndLng)

                            Polyline(
                                points = listOf(activeBus.latLng, currentEndLatLng),
                                color = Color(0xFF1565C0),
                                width = 12f
                            )

                            Marker(
                                state = MarkerState(position = activeBus.latLng),
                                title = "🚌 ${activeBus.name} (${activeBus.tag})",
                                snippet = "Sentido: SUBIDA ➔ Hacia Saylla | Cond: ${activeBus.driverName}",
                                icon = busIconDescriptor
                            )

                            Marker(
                                state = MarkerState(position = userLatLng),
                                title = "📍 Tu Paradero: Manuel Prado",
                                snippet = "Hora de llegada: $arrivalTimeStr (🟢 En camino)",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )
                        }
                        else -> {
                            cuscoParaderosReales.forEach { paradero ->
                                Marker(
                                    state = MarkerState(position = paradero.location),
                                    title = paradero.name,
                                    snippet = paradero.snippet,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )
                            }
                        }
                    }
                }

                if (isDriver) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onOpenMenu) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menú Principal",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Icon(
                                Icons.Filled.DirectionsBus,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "● MODO CONDUCTOR ACTIVO",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Empresa: $company | Ruta: $route",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onThemeChanged(!isDarkMode) }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Alternar Modo Oscuro",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onOpenMenu) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menú Principal",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                TextField(
                                    value = mapSearchQuery,
                                    onValueChange = { mapSearchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Buscar paradero o lugar...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (mapSearchQuery.isNotBlank()) {
                                IconButton(onClick = { mapSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Limpiar Búsqueda",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { onThemeChanged(!isDarkMode) }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Alternar Modo Oscuro",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}