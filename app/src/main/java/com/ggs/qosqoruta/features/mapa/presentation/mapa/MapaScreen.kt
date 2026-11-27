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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
import com.ggs.qosqoruta.features.mapa.data.*
import com.ggs.qosqoruta.features.mapa.domain.models.BusInfo
import com.ggs.qosqoruta.features.mapa.presentation.components.EnVivoBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.ExplorarBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.FavoritosBottomSheetContent
import com.ggs.qosqoruta.features.mapa.presentation.components.RutasBottomSheetContent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
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
fun getCustomBusIconDescriptor(
    context: Context,
    routeColor: Color = Color(0xFF00875A),
    unitCode: String = "BUS"
): BitmapDescriptor {
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
            color = routeColor.toArgb()
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val bodyRect = RectF(8f, 8f, 102f, 54f)
        canvas.drawRoundRect(bodyRect, 16f, 16f, fillPaint)
        canvas.drawRoundRect(bodyRect, 16f, 16f, strokePaint)

        val windowRect = RectF(16f, 16f, 94f, 32f)
        canvas.drawRoundRect(windowRect, 6f, 6f, strokePaint)

        val labelPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 16f
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }
        canvas.drawText(unitCode.takeLast(4), 55f, 47f, labelPaint)

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

@Composable
private fun BusMapMarker(
    bus: BusInfo,
    icon: BitmapDescriptor,
    onClick: () -> Unit
) {
    val markerState = remember(bus.id) { MarkerState(position = bus.latLng) }

    LaunchedEffect(bus.id, bus.latLng) {
        val start = markerState.position
        val frames = 10
        for (frame in 1..frames) {
            val fraction = frame / frames.toFloat()
            markerState.position = LatLng(
                start.latitude + (bus.latLng.latitude - start.latitude) * fraction,
                start.longitude + (bus.latLng.longitude - start.longitude) * fraction
            )
            delay(50)
        }
    }

    Marker(
        state = markerState,
        title = "Unidad ${bus.id} · ${bus.name}",
        snippet = "${bus.etaMinutes} min · ${bus.nextStopName} · DEMO",
        icon = icon,
        rotation = (bus.bearingDegrees - 90f + 360f) % 360f,
        onClick = {
            onClick()
            true
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onOpenMenu: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val userRole = sessionManager.getUserRole()
    val isDriver = userRole == UserRole.CONDUCTOR
    val company = sessionManager.getDriverCompany()
    val route = sessionManager.getDriverRoute()

    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0 = Explorar, 1 = Rutas, 2 = En Vivo, 3 = Favoritos
    var selectedRouteTag by remember { mutableStateOf("RTI-07") }
    val selectedRouteConfig = rutaMapaConfig(selectedRouteTag)
    val animatedRouteColor by animateColorAsState(
        targetValue = selectedRouteConfig.uiColor(isDarkMode),
        animationSpec = tween(durationMillis = 350),
        label = "selectedRouteColor"
    )
    val animatedRouteLineColor by animateColorAsState(
        targetValue = selectedRouteConfig.color,
        animationSpec = tween(durationMillis = 350),
        label = "selectedRouteLineColor"
    )

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
    val fleetStore = remember { LocalFleetStore(initialFavs) }
    val busesList by fleetStore.buses.collectAsState()
    var activeBusId by remember { mutableStateOf("B1") }
    val activeBus = busesList.firstOrNull { it.id == activeBusId } ?: busesList.first()
    val busIconDescriptors = remember(context, busesList.map { it.id }) {
        busesList.associate { bus ->
            bus.id to getCustomBusIconDescriptor(
                context,
                rutaMapaConfig(bus.tag).color,
                bus.id
            )
        }
    }

    LaunchedEffect(fleetStore) {
        while (true) {
            delay(1_000)
            fleetStore.advanceSimulation()
        }
    }
    val animatedActiveBusRouteColor by animateColorAsState(
        targetValue = rutaMapaConfig(activeBus.tag).uiColor(isDarkMode),
        animationSpec = tween(durationMillis = 350),
        label = "activeBusRouteColor"
    )
    val animatedActiveBusLineColor by animateColorAsState(
        targetValue = rutaMapaConfig(activeBus.tag).color,
        animationSpec = tween(durationMillis = 350),
        label = "activeBusLineColor"
    )

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

    val focusBus: (BusInfo) -> Unit = { bus ->
        activeBusId = bus.id
        selectedRouteTag = rutaMapaConfig(bus.tag).code
        scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(bus.latLng, 16f),
                durationMs = 900
            )
        }
    }

    LaunchedEffect(selectedBottomTab) {
        fleetStore.updateFavorites(sessionManager.getFavoriteBusIds())
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    val toggleFavorite: (String) -> Unit = { busId ->
        val updatedFavs = sessionManager.toggleFavoriteBus(busId)
        fleetStore.updateFavorites(updatedFavs)
    }

    val removeFavorite: (String) -> Unit = { busId ->
        val updatedFavs = sessionManager.removeFavoriteBus(busId)
        fleetStore.updateFavorites(updatedFavs)
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = animatedRouteColor,
                        selectedTextColor = animatedRouteColor,
                        indicatorColor = animatedRouteColor.copy(alpha = 0.14f)
                    ),
                    onClick = {
                        selectedBottomTab = 0
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.Filled.DirectionsBus, contentDescription = "Explorar") },
                    label = { Text("Explorar", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = animatedRouteColor,
                        selectedTextColor = animatedRouteColor,
                        indicatorColor = animatedRouteColor.copy(alpha = 0.14f)
                    ),
                    onClick = {
                        selectedBottomTab = 1
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = "Rutas") },
                    label = { Text("Rutas", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = animatedRouteColor,
                        selectedTextColor = animatedRouteColor,
                        indicatorColor = animatedRouteColor.copy(alpha = 0.14f)
                    ),
                    onClick = {
                        selectedBottomTab = 2
                        scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                    },
                    icon = { Icon(Icons.Filled.Navigation, contentDescription = "En Vivo") },
                    label = { Text("En Vivo", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = animatedRouteColor,
                        selectedTextColor = animatedRouteColor,
                        indicatorColor = animatedRouteColor.copy(alpha = 0.14f)
                    ),
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
                            focusBus(bus)
                            selectedRouteTag = rutaMapaConfig(bus.tag).code
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onFollowBusClick = { bus ->
                            focusBus(bus)
                            selectedRouteTag = rutaMapaConfig(bus.tag).code
                            selectedBottomTab = 2
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
                        isDarkMode = isDarkMode,
                        buses = busesList,
                        onSelectRoute = { routeCode ->
                            selectedRouteTag = routeCode
                            val routeConfig = rutaMapaConfig(routeCode)
                            busesList
                                .filter { it.tag in routeConfig.busTags }
                                .minByOrNull { it.etaMinutes }
                                ?.let { activeBusId = it.id }
                            scope.launch {
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onBusSelected = { bus ->
                            focusBus(bus)
                        }
                    )
                    2 -> EnVivoBottomSheetContent(
                        activeBus = activeBus,
                        routeColor = animatedActiveBusRouteColor,
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
                            focusBus(bus)
                            selectedRouteTag = rutaMapaConfig(bus.tag).code
                            selectedBottomTab = 2
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
                                BusMapMarker(
                                    bus = bus,
                                    icon = busIconDescriptors[bus.id]
                                        ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                                    onClick = { focusBus(bus) }
                                )
                            }
                        }
                        1 -> {
                            Polyline(
                                points = selectedRouteConfig.polyline,
                                color = animatedRouteLineColor,
                                width = 14f,
                                startCap = RoundCap(),
                                endCap = RoundCap()
                            )

                            val routeParaderos = cuscoParaderosReales.filter { paradero ->
                                paradero.snippet.contains(selectedRouteConfig.stopSnippet, ignoreCase = true)
                            }
                            routeParaderos.forEach { paradero ->
                                Marker(
                                    state = MarkerState(position = paradero.location),
                                    title = paradero.name,
                                    snippet = paradero.snippet,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )
                            }

                            val routeBuses = busesList.filter { bus -> bus.tag in selectedRouteConfig.busTags }
                            routeBuses.forEach { bus ->
                                BusMapMarker(
                                    bus = bus,
                                    icon = busIconDescriptors[bus.id]
                                        ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                                    onClick = { focusBus(bus) }
                                )
                            }
                        }
                        2 -> {
                            val activeRouteConfig = rutaMapaConfig(activeBus.tag)
                            Polyline(
                                points = activeRouteConfig.polyline,
                                color = animatedActiveBusLineColor.copy(alpha = 0.42f),
                                width = 8f,
                                startCap = RoundCap(),
                                endCap = RoundCap()
                            )
                            busesList
                                .filter { it.tag in activeRouteConfig.busTags }
                                .forEach { bus ->
                                    BusMapMarker(
                                        bus = bus,
                                        icon = busIconDescriptors[bus.id]
                                            ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                                        onClick = { focusBus(bus) }
                                    )
                                }

                            val nextStop = cuscoParaderosReales.firstOrNull {
                                it.name == activeBus.nextStopName
                            }
                            val targetLatLng = nextStop?.location ?: userLatLng
                            val calendar = Calendar.getInstance().apply {
                                add(Calendar.MINUTE, activeBus.etaMinutes)
                            }
                            val arrivalTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

                            val currentEndLat = activeBus.latLng.latitude + (targetLatLng.latitude - activeBus.latLng.latitude) * blueLineProgress
                            val currentEndLng = activeBus.latLng.longitude + (targetLatLng.longitude - activeBus.latLng.longitude) * blueLineProgress
                            val currentEndLatLng = LatLng(currentEndLat, currentEndLng)

                            Polyline(
                                points = listOf(activeBus.latLng, currentEndLatLng),
                                color = animatedActiveBusLineColor,
                                width = 12f,
                                startCap = RoundCap(),
                                endCap = RoundCap()
                            )

                            Marker(
                                state = MarkerState(position = targetLatLng),
                                title = "📍 ${activeBus.nextStopName}",
                                snippet = "Llegada estimada: $arrivalTimeStr · ${activeBus.etaMinutes} min · DEMO",
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
                        color = lerp(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            animatedRouteColor,
                            0.10f
                        ),
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                        tint = animatedRouteColor
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
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = animatedRouteColor
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                cuscoRutasMapa.forEach { config ->
                                    val isSelected = selectedRouteConfig.code == config.code
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedRouteTag = config.code
                                            busesList
                                                .filter { it.tag in config.busTags }
                                                .minByOrNull { it.etaMinutes }
                                                ?.let { activeBusId = it.id }
                                            selectedBottomTab = 1
                                            scope.launch {
                                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                            }
                                        },
                                        label = { Text(config.displayName, maxLines = 1) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Circle,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            iconColor = config.uiColor(isDarkMode),
                                            selectedContainerColor = config.uiColor(isDarkMode).copy(alpha = 0.14f),
                                            selectedLabelColor = config.uiColor(isDarkMode),
                                            selectedLeadingIconColor = config.uiColor(isDarkMode)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
