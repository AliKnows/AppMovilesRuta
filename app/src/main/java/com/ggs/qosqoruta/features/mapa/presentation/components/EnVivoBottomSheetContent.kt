package com.ggs.qosqoruta.features.mapa.presentation.components

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ggs.qosqoruta.features.mapa.domain.models.BusInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EnVivoBottomSheetContent(
    activeBus: BusInfo,
    routeColor: Color,
    onStopFollowingClick: () -> Unit
) {
    val context = LocalContext.current

    var currentStepIndex by remember { mutableIntStateOf(1) } // 0 = En camino, 1 = Próximo paradero, 2 = En tu paradero

    val etaMinutes = activeBus.etaMinutes.coerceAtLeast(1)
    val arrivalClockTime = remember(etaMinutes) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, etaMinutes)
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    }

    LaunchedEffect(activeBus.id) {
        currentStepIndex = 1
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. RESUMEN DE LA UNIDAD Y SU ETA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = lerp(MaterialTheme.colorScheme.surface, routeColor, 0.12f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, routeColor.copy(alpha = 0.28f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = routeColor,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (activeBus.isDemo) "DEMO · SIMULACIÓN LOCAL" else "TRANSMISIÓN EN VIVO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = routeColor
                        )
                    }

                    // TOOLTIP FLOTANTE TIPO RELOJ DE LLEGADA (10:11 am)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Hora de llegada",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = arrivalClockTime,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = routeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeBus.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${activeBus.tag} • ${activeBus.route}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = routeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = etaMinutes.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = routeColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "min • ETA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Próximo paradero: ${activeBus.nextStopName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BANDEROLA DE SENTIDO DE MARCHA (SUBIDA / BAJADA) Y DIRECCIÓN
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NorthEast,
                            contentDescription = null,
                            tint = routeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rumbo ${activeBus.bearingDegrees.toInt()}°",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = routeColor
                            )
                            Text(
                                text = "Siguiente parada: ${activeBus.nextStopName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. MONITOREO DE PARADEROS EN TIEMPO REAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Avance del Conductor por Paraderos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Parada 1: Conductor transitando
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = routeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ubicación del Conductor: En tránsito", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("En ruta ${activeBus.tag}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Parada 2: Próximo paradero del chofer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentStepIndex >= 1) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = routeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Siguiente Parada: ${activeBus.nextStopName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(activeBus.distance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Parada 3: Paradero cercano del Pasajero
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentStepIndex >= 2) Icons.Filled.Place else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (currentStepIndex >= 2) routeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tu Paradero Cercano: ${activeBus.nextStopName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = routeColor)
                        Text("Punto de abordaje (Llegada a las $arrivalClockTime)", style = MaterialTheme.typography.bodySmall, color = routeColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 3. TARJETA DE INFORMACIÓN DE UNIDAD Y CONDUCTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeBus.driverName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (activeBus.isDemo) {
                                        Icons.Filled.DirectionsBus
                                    } else {
                                        Icons.Filled.Verified
                                    },
                                    contentDescription = if (activeBus.isDemo) {
                                        "Unidad de demostración"
                                    } else {
                                        "Conductor Verificado"
                                    },
                                    tint = routeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (activeBus.isDemo) {
                                    "Unidad de demostración local"
                                } else {
                                    "Conductor Verificado QosQoRuta"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = routeColor
                            )
                        }
                    }

                    if (!activeBus.isDemo) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = routeColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4.9", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Ocupación de Unidad", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeBus.occupancy, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = activeBus.occupancyColor)
                    }
                    Column {
                        Text("Pasaje Exacto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeBus.price, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Frecuencia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeBus.frequency, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = routeColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN COMPARTIR UBICACIÓN EN VIVO (ANCHO COMPLETO)
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            val message = if (activeBus.isDemo) {
                                "Simulación local de ${activeBus.name} (${activeBus.tag}) en QosQoRuta: ETA de ${activeBus.etaMinutes} min."
                            } else {
                                "Sigue mi bus en vivo (${activeBus.name} - ${activeBus.tag}) en QosQoRuta Cusco: Llegando a mi paradero a las $arrivalClockTime."
                            }
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir Viaje en Vivo"))
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = routeColor)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (activeBus.isDemo) "Compartir ETA de demostración" else "Compartir Ubicación del Viaje en Vivo")
                }
            }
        }

        // 4. BOTÓN DETENER SEGUIMIENTO
        OutlinedButton(
            onClick = onStopFollowingClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Detener Seguimiento en Vivo", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
