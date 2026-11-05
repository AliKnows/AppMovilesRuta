package com.ggs.qosqoruta.features.mapa.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class EmpresaDetalle(
    val code: String,
    val name: String,
    val companyName: String,
    val itinerary: String,
    val totalStops: Int,
    val price: String,
    val frequency: String,
    val routeType: String,
    val stopsList: List<String>
)

val cuscoEmpresasInscritas = listOf(
    EmpresaDetalle(
        code = "RTI-07",
        name = "Expreso Batman",
        companyName = "Empresa Nuevo Mirador S.A.",
        itinerary = "Ccachona ➔ Huancaro ➔ Óvalo Pachacútec ➔ Terminal ➔ San Sebastián ➔ UAC ➔ Saylla",
        totalStops = 19,
        price = "S/ 1.00",
        frequency = "Cada 4 min",
        routeType = "Eje Diagonal",
        stopsList = listOf(
            "Final Ccachona (Inicio)", "Octavo Paradero (Cachona)", "Mercado de Huancaro", "Belempampa",
            "Colegio Rosario", "Óvalo Pachacútec", "Terminal Terrestre (Ingreso)", "Coliseo Uriel García (Ttito)",
            "Ingreso Vía Expresa", "República de Brasil", "Primer Paradero San Sebastián", "Plaza de San Sebastián",
            "Séptimo Paradero San Sebastián", "Mercadillo San Sebastián", "Universidad Andina (UAC)",
            "Penal (San Jerónimo)", "Mercado Vinocanchón", "La Encantada / Angostura", "Estadio de Saylla (Final)"
        )
    ),
    EmpresaDetalle(
        code = "RTU-01",
        name = "Patrón de San Jerónimo",
        companyName = "Empresa Patrón de San Jerónimo S.A.",
        itinerary = "Mirador Yupanqui ➔ Plaza Santiago ➔ Puente Grau ➔ UNSAAC ➔ Magisterio ➔ Univ. Continental",
        totalStops = 17,
        price = "S/ 1.50",
        frequency = "Cada 6 min",
        routeType = "Clúster Académico",
        stopsList = listOf(
            "Mirador Domingo Yupanqui (Inicio)", "Mercado de Huancaro", "Plaza Santiago", "Puente Grau",
            "Calle San Miguel", "Paradero Garcilaso", "UNSAAC (Puerta Principal)", "Hospital Regional",
            "Manuel Prado", "Magisterio", "Marcavalle", "Primer Paradero San Sebastián",
            "Séptimo Paradero San Sebastián", "Paradero Penal", "Mercado Vinocanchón", "Ciencias de la Salud UAC",
            "Universidad Continental (Final)"
        )
    ),
    EmpresaDetalle(
        code = "RTI-01",
        name = "Conector Valle Sur",
        companyName = "Empresa Saylla Huasao",
        itinerary = "Luis Vallejo Santoni (Zarzuela) ➔ Mercado Wanchaq ➔ UNSAAC ➔ Hospital Regional ➔ Huasao",
        totalStops = 14,
        price = "S/ 1.00",
        frequency = "Cada 8 min",
        routeType = "Conector Valle Sur",
        stopsList = listOf(
            "Luis Vallejo Santoni (Zarzuela)", "Mercado Wanchaq", "Mariscal Gamarra", "UNSAAC",
            "Hospital Regional", "Primer Paradero San Sebastián", "Séptimo Paradero San Sebastián",
            "Universidad Andina (UAC)", "Paradero Penal", "Mercado Vinocanchón", "Ciencias de la Salud UAC",
            "La Encantada / Angostura", "Estadio de Saylla", "Huasao (Terminal Valle Sur Final)"
        )
    ),
    EmpresaDetalle(
        code = "LEONES",
        name = "Los Leones",
        companyName = "Empresa Los Leones",
        itinerary = "Plaza de Tipón ➔ Huasao ➔ UAC ➔ UNSAAC ➔ Limacpampa ➔ Av. El Sol ➔ Saphi (Don Bosco)",
        totalStops = 13,
        price = "S/ 1.50",
        frequency = "Cada 10 min",
        routeType = "Penetración Centro Histórico",
        stopsList = listOf(
            "Plaza de Tipón / Choquepata (Inicio)", "Huasao", "Estadio de Saylla", "Ciencias de la Salud UAC",
            "Paradero Penal", "Universidad Andina (UAC)", "Séptimo Paradero San Sebastián", "UNSAAC",
            "Limacpampa", "Avenida El Sol (Maruri)", "Plaza San Francisco", "Santa Teresa", "Don Bosco / Saphi (Final)"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineasEmpresasScreen(
    onOpenMenu: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    var expandedCompanyCode by remember { mutableStateOf<String?>("RTI-07") }

    Scaffold(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Líneas y Empresas") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú Principal")
                    }
                },
                actions = {
                    IconButton(onClick = { onThemeChanged(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Alternar Modo Oscuro"
                        )
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Empresas Inscritas en QosQoRuta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${cuscoEmpresasInscritas.size} empresas registradas con paraderos oficiales en Cusco",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            cuscoEmpresasInscritas.forEach { empresa ->
                val isExpanded = expandedCompanyCode == empresa.code

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedCompanyCode = if (isExpanded) null else empresa.code
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
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
                                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                        contentDescription = null,
                                        tint = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${empresa.code} • ${empresa.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = empresa.companyName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Desplegar"
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = empresa.itinerary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(8.dp)) {
                                    Text(empresa.frequency, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                }
                                Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(8.dp)) {
                                    Text(empresa.price, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "${empresa.totalStops} Paraderos",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // VISTA DESPLEGABLE CON LISTA COMPLETA DE PARADEROS
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                                Text(
                                    text = "Paraderos de la Ruta (${empresa.totalStops} paradas oficiales):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                empresa.stopsList.forEachIndexed { index, stopName ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = stopName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
