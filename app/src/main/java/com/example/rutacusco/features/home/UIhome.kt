package com.example.rutacusco.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rutacusco.ui.theme.IconoAzul
import com.example.rutacusco.ui.theme.IconoGris
import com.example.rutacusco.ui.theme.IconoNaranja
import com.example.rutacusco.ui.theme.IconoPurpura
import com.example.rutacusco.ui.theme.IconoVerde
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIhome(
    onNavigateToExplorar: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // --- Encabezado del Menú con botón de Cierre ('X') ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QosqoRuta",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pasajero Andino",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Combis y Buses",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar menú",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // --- Opciones Principales del Menú con sus Colores ---
                NavigationDrawerItem(
                    label = { Text("Modo Pasajero") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = IconoAzul) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Líneas y Empresas de Cusco") },
                    icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = IconoAzul) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Paraderos Cercanos") },
                    icon = { Icon(Icons.Default.NearMe, contentDescription = null, tint = IconoAzul) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                // Tarifas (Naranja)
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text("Tarifas y Pasajes")
                            Text(
                                text = "Adulto S/. 1.00 • Est. S/. 0.50",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = IconoNaranja) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                // Historial (Púrpura)
                NavigationDrawerItem(
                    label = { Text("Historial de Rutas") },
                    icon = { Icon(Icons.Default.History, contentDescription = null, tint = IconoPurpura) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                // Offline (Verde)
                NavigationDrawerItem(
                    label = { Text("Modo Desconectado (Offline)") },
                    icon = { Icon(Icons.Default.SignalCellularConnectedNoInternet4Bar, contentDescription = null, tint = IconoVerde) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Configuración (Gris)
                NavigationDrawerItem(
                    label = { Text("Configuración") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = IconoGris) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Bus Cusco",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bienvenido a Cusco",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onNavigateToExplorar) {
                    Text("Explorar Destinos")
                }
            }
        }
    }
}