package com.ggs.qosqoruta.features.mapa.presentation.configuracion

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ggs.qosqoruta.core.database.QosQoRutaDatabase
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.core.security.PasswordSecurity
import com.ggs.qosqoruta.features.auth.domain.models.UserRole
import com.ggs.qosqoruta.features.mapa.presentation.components.ProfileAvatarImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onOpenMenu: () -> Unit, 
    onCerrarSesion: () -> Unit,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onNavigateToMap: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentRole by remember { mutableStateOf(sessionManager.getUserRole()) }

    var nameInput by remember { mutableStateOf(sessionManager.getUserName()) }
    var surnameInput by remember { mutableStateOf(sessionManager.getUserSurname()) }
    var phoneInput by remember { mutableStateOf(sessionManager.getUserPhone()) }
    var avatarId by remember { mutableIntStateOf(sessionManager.getAvatarId()) }
    var userAvatarUri by remember { mutableStateOf(sessionManager.getAvatarUri()) }

    var showAvatarDialog by remember { mutableStateOf(false) }

    // SELECCIONADOR DE FOTO DE LA GALERÍA DEL TELÉFONO
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val uriStr = uri.toString()
            userAvatarUri = uriStr
            sessionManager.setAvatarUri(uriStr)
            showAvatarDialog = false
        }
    }

    var showPasswordSection by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var notificacionesPush by remember { mutableStateOf(true) }
    var showDriverDialog by remember { mutableStateOf(false) }

    var selectedCompany by remember { mutableStateOf("Nuevo Mirador") }
    var selectedRoute by remember { mutableStateOf("RTI-07") }
    var expandedCompany by remember { mutableStateOf(false) }
    var expandedRoute by remember { mutableStateOf(false) }

    val companies = listOf("Nuevo Mirador", "Patrón San Jerónimo", "Saylla Huasao")
    val routesMap = mapOf(
        "Nuevo Mirador" to listOf("RTI-07"),
        "Patrón San Jerónimo" to listOf("RTU-01"),
        "Saylla Huasao" to listOf("RTI-01")
    )
    val currentRoutes = routesMap[selectedCompany] ?: emptyList()

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Cambiar Foto de Perfil") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📁 Subir Foto de la Galería")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("O selecciona un avatar predefinido:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val avatarColors = listOf(
                            MaterialTheme.colorScheme.primaryContainer to "Estándar",
                            MaterialTheme.colorScheme.secondaryContainer to "Conductor",
                            MaterialTheme.colorScheme.tertiaryContainer to "Pasajero"
                        )
                        avatarColors.forEachIndexed { index, (containerColor, label) ->
                            Surface(
                                shape = CircleShape,
                                color = containerColor,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable {
                                        avatarId = index
                                        userAvatarUri = null
                                        sessionManager.setAvatarUri(null)
                                        showAvatarDialog = false
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, contentDescription = label, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showDriverDialog) {
        AlertDialog(
            onDismissRequest = { showDriverDialog = false },
            title = { Text("Datos de Conductor") },
            text = {
                Column {
                    Text("Selecciona tu empresa y ruta de hoy:")
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedCompany,
                        onExpandedChange = { expandedCompany = !expandedCompany },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedCompany,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Empresa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCompany) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCompany,
                            onDismissRequest = { expandedCompany = false }
                        ) {
                            companies.forEach { companyOption ->
                                DropdownMenuItem(
                                    text = { Text(companyOption) },
                                    onClick = {
                                        selectedCompany = companyOption
                                        selectedRoute = routesMap[companyOption]?.firstOrNull() ?: ""
                                        expandedCompany = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedRoute,
                        onExpandedChange = { expandedRoute = !expandedRoute },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRoute,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ruta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoute) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRoute,
                            onDismissRequest = { expandedRoute = false }
                        ) {
                            currentRoutes.forEach { routeOption ->
                                DropdownMenuItem(
                                    text = { Text(routeOption) },
                                    onClick = {
                                        selectedRoute = routeOption
                                        expandedRoute = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedCompany.isNotBlank() && selectedRoute.isNotBlank()) {
                        sessionManager.updateRoleToDriver(selectedCompany, selectedRoute)
                        currentRole = UserRole.CONDUCTOR
                        showDriverDialog = false
                        onNavigateToMap()
                    }
                }) { Text("Aceptar y Ver Mapa") }
            },
            dismissButton = {
                TextButton(onClick = { showDriverDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Cuenta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatarImage(
                        avatarUri = userAvatarUri,
                        avatarId = avatarId,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { showAvatarDialog = true },
                        iconSize = 44.dp
                    )
                    SmallFloatingActionButton(
                        onClick = { showAvatarDialog = true },
                        modifier = Modifier.size(26.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Cambiar Foto", modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (surnameInput.isBlank()) nameInput else "$nameInput $surnameInput",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (currentRole == UserRole.CONDUCTOR) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = currentRole.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = surnameInput,
                onValueChange = { surnameInput = it },
                label = { Text("Apellido") },
                leadingIcon = { Icon(Icons.Filled.PersonOutline, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showPasswordSection = !showPasswordSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar Contraseña", fontWeight = FontWeight.Bold)
                        }
                        Icon(
                            imageVector = if (showPasswordSection) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showPasswordSection) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Contraseña Actual") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva Contraseña") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar Nueva Contraseña") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (oldPassword.isNotBlank() || newPassword.isNotBlank()) {
                            if (newPassword != confirmPassword) {
                                snackbarHostState.showSnackbar("Las nuevas contraseñas no coinciden")
                                return@launch
                            }
                            if (newPassword.length < 6) {
                                snackbarHostState.showSnackbar("La nueva contraseña debe tener al menos 6 caracteres")
                                return@launch
                            }

                            val database = QosQoRutaDatabase.getDatabase(context)
                            val user = database.authDao().getUserByEmail("admin@qosqoruta.com") ?: database.authDao().getUserByEmail("juan.lizarraga@qosqoruta.com")
                            if (user != null && user.passwordHash.isNotBlank()) {
                                val isOldValid = PasswordSecurity.verifyPassword(oldPassword, user.passwordHash)
                                if (!isOldValid) {
                                    snackbarHostState.showSnackbar("La contraseña actual es incorrecta")
                                    return@launch
                                }
                                val newHash = PasswordSecurity.hashPassword(newPassword)
                                database.authDao().updateUser(user.copy(name = "$nameInput $surnameInput".trim(), passwordHash = newHash))
                            }
                        }

                        sessionManager.updateFullProfile(nameInput, surnameInput, phoneInput, avatarId, userAvatarUri)
                        snackbarHostState.showSnackbar("¡Perfil guardado con éxito!")
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        showPasswordSection = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Preferencias", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    if (currentRole == UserRole.PASAJERO) {
                        showDriverDialog = true
                    } else {
                        sessionManager.updateRoleToPassenger()
                        currentRole = UserRole.PASAJERO
                        onNavigateToMap()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (currentRole == UserRole.PASAJERO) Icons.Filled.DirectionsCar else Icons.Filled.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentRole == UserRole.PASAJERO) "Cambiar a Modo Conductor" else "Volver a Modo Pasajero")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Acerca de", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Versión de la aplicación: 1.0.0 (Alpha)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

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
