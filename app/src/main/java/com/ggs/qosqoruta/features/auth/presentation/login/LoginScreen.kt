package com.ggs.qosqoruta.features.auth.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.ggs.qosqoruta.R
import com.ggs.qosqoruta.features.auth.presentation.components.QosQoRutaLogo
import com.ggs.qosqoruta.features.auth.presentation.state.AuthUiState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Selector de Rol (Pasajero/Conductor)
    var isDriver by remember { mutableStateOf(false) }

    // Estados para el diálogo posterior si faltan datos de conductor
    var showDriverDialog by remember { mutableStateOf(false) }
    var driverDialogEmail by remember { mutableStateOf("") }
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

    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                showDriverDialog = false
                onLoginSuccess()
                viewModel.resetState()
            }
            is AuthUiState.NeedsDriverInfo -> {
                driverDialogEmail = state.email
                showDriverDialog = true
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val onLoginClick: () -> Unit = {
        if (email.isBlank() || password.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Por favor, ingresa correo y contraseña") }
        } else {
            focusManager.clearFocus()
            viewModel.loginLocal(
                email = email,
                passwordRaw = password,
                selectedRole = if (isDriver) "CONDUCTOR" else "PASAJERO"
            )
        }
    }

    // Diálogo emergente si faltan datos de conductor tras el login
    if (showDriverDialog) {
        AlertDialog(
            onDismissRequest = { /* Forzar selección */ },
            title = { Text("Datos de Conductor") },
            text = {
                Column {
                    Text("Iniciaste como Conductor. Para continuar, selecciona tu empresa y ruta de hoy:")
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
                Button(
                    onClick = {
                        if (selectedCompany.isNotBlank() && selectedRoute.isNotBlank()) {
                            viewModel.completeDriverInfo(
                                email = driverDialogEmail,
                                company = selectedCompany,
                                route = selectedRoute
                            )
                        }
                    }
                ) {
                    Text("Guardar y Continuar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo de la Empresa (Diseño Vectorial + Tipografía)
                item {
                    QosQoRutaLogo(
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                    )
                }

                item {
                    Text(
                        text = "Inicia sesión para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }

                // Selector de Rol (SegmentedButton con íconos Pasajero/Conductor)
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        SegmentedButton(
                            selected = !isDriver,
                            onClick = { isDriver = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) {
                            Text("Pasajero")
                        }
                        SegmentedButton(
                            selected = isDriver,
                            onClick = { isDriver = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsBus,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) {
                            Text("Conductor")
                        }
                    }
                }

                // Campo Correo
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !isLoading,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Campo Contraseña
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Alternar visibilidad"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onLoginClick() }
                        ),
                        enabled = !isLoading,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }

                // Botones de Inicio de Sesión
                item {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        Button(
                            onClick = { onLoginClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Iniciar Sesión", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Continuar con Google (Logo Oficial)
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId("498630551074-ueorsv8vp4q4b2fo5h4rco0jjq2adr1m.apps.googleusercontent.com")
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(context, request)
                                        val credential = result.credential

                                        if (credential is CustomCredential &&
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        ) {
                                            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                                            val googleEmail = googleCred.id
                                            val googleName = googleCred.displayName ?: googleCred.givenName ?: "Usuario Google"

                                            viewModel.handleGoogleLoginSuccess(
                                                email = googleEmail,
                                                displayName = googleName,
                                                selectedRole = if (isDriver) "CONDUCTOR" else "PASAJERO"
                                            )
                                        } else {
                                            viewModel.handleGoogleLoginError("Tipo de credencial no soportado")
                                        }
                                    } catch (e: GetCredentialException) {
                                        viewModel.handleGoogleLoginError("Cancelado o error: ${e.localizedMessage}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Logo de Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continuar con Google", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !isLoading,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("¿No tienes cuenta? Regístrate aquí")
                    }
                }

                item {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            email = "admin@qosqoruta.com"
                            password = "admin123"
                            viewModel.loginLocal(
                                email = "admin@qosqoruta.com",
                                passwordRaw = "admin123",
                                selectedRole = if (isDriver) "CONDUCTOR" else "PASAJERO"
                            )
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Ingreso Rápido (Modo Admin)",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}