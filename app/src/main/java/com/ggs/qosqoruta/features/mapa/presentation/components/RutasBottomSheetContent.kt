package com.ggs.qosqoruta.features.mapa.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.mapa.data.cuscoRutasList
import com.ggs.qosqoruta.features.mapa.data.rutaMapaConfig
import com.ggs.qosqoruta.features.mapa.data.uiColor
import com.ggs.qosqoruta.features.mapa.domain.models.BusInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutasBottomSheetContent(
    selectedRouteTag: String,
    isDarkMode: Boolean,
    buses: List<BusInfo>,
    onBusSelected: (BusInfo) -> Unit,
    onSelectRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var expandedRouteCode by remember { mutableStateOf<String?>("RTI-07") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Líneas y Rutas de Cusco",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Elige una línea para ver su recorrido y paraderos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val selectedRoute = rutaMapaConfig(selectedRouteTag)
        val routeBuses = buses
            .filter { it.tag in selectedRoute.busTags }
            .sortedWith(compareBy<BusInfo> { it.etaMinutes }.thenBy { it.id })
        val selectedColor = selectedRoute.uiColor(isDarkMode)

        Text(
            text = "ETA de ${selectedRoute.displayName} · DEMO LOCAL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = selectedColor
        )
        routeBuses.forEachIndexed { index, bus ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBusSelected(bus) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = selectedColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "Rumbo ${bus.bearingDegrees.toInt()} grados",
                            tint = selectedColor,
                            modifier = Modifier
                                .padding(10.dp)
                                .rotate(bus.bearingDegrees)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (index) {
                                0 -> "PRÓXIMO · ${bus.id}"
                                1 -> "SIGUIENTE · ${bus.id}"
                                else -> "UNIDAD ${index + 1} · ${bus.id}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = selectedColor
                        )
                        Text(
                            text = bus.nextStopName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "${bus.distance} · Rumbo ${bus.bearingDegrees.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${bus.etaMinutes}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = selectedColor
                        )
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        cuscoRutasList.forEach { ruta ->
            val routeConfig = rutaMapaConfig(ruta.code)
            val routeColor = routeConfig.uiColor(isDarkMode)
            val onRouteColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
            val isSelected = selectedRouteTag == ruta.code
            val isExpanded = expandedRouteCode == ruta.code

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    .clickable {
                        sessionManager.addSearchHistory(ruta.itinerary, "Línea: ${ruta.code}")
                        onSelectRoute(ruta.code)
                        expandedRouteCode = if (isExpanded) null else ruta.code
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) routeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) routeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = if (isSelected) onRouteColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${ruta.code} • ${ruta.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ruta.company,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = routeColor
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Surface(
                                    color = routeColor,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "SELECCIONADA",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onRouteColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Desplegar"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = routeColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ruta.itinerary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Descripción del Recorrido:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = routeColor
                            )
                            Text(
                                text = ruta.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(8.dp)) {
                                        Text(ruta.frequency, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(8.dp)) {
                                        Text(ruta.price, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = "${ruta.activeBusesCount} unidades activas",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = routeColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
