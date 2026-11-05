package com.ggs.qosqoruta.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.repositories.IAuthRepository
import com.ggs.qosqoruta.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: IAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 1. LOGIN LOCAL
    fun loginLocal(
        email: String,
        passwordRaw: String,
        selectedRole: String
    ) {
        // Bypass para Modo Admin
        if (email == "admin@qosqoruta.com" && passwordRaw == "admin123") {
            val company = if (selectedRole == "CONDUCTOR") "Nuevo Mirador" else null
            val route = if (selectedRole == "CONDUCTOR") "RTI-07" else null
            sessionManager.saveSession(
                name = "Administrador",
                role = selectedRole,
                company = company,
                route = route,
                email = email
            )
            _uiState.value = AuthUiState.Success
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.loginLocalUser(email, passwordRaw)
            when (result) {
                is AuthResult.Success -> {
                    val effectiveRole = selectedRole // RESPETA ESTRICTAMENTE LA SELECCIÓN DEL USUARIO (PASAJERO / CONDUCTOR)

                    if (effectiveRole == "CONDUCTOR") {
                        val hasDriverInfo = !result.company.isNullOrBlank() && !result.routeName.isNullOrBlank()
                        if (!hasDriverInfo) {
                            _uiState.value = AuthUiState.NeedsDriverInfo(
                                email = email,
                                name = result.name,
                                role = "CONDUCTOR"
                            )
                        } else {
                            sessionManager.saveSession(
                                name = result.name,
                                role = "CONDUCTOR",
                                company = result.company,
                                route = result.routeName,
                                email = email
                            )
                            _uiState.value = AuthUiState.Success
                        }
                    } else {
                        sessionManager.saveSession(
                            name = result.name,
                            role = "PASAJERO",
                            company = null,
                            route = null,
                            email = email
                        )
                        _uiState.value = AuthUiState.Success
                    }
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // 2. LOGIN GOOGLE
    fun handleGoogleLoginSuccess(email: String, displayName: String, selectedRole: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.loginOrRegisterGoogleUser(email, displayName, selectedRole)
            when (result) {
                is AuthResult.Success -> {
                    val effectiveRole = selectedRole // RESPETA ESTRICTAMENTE LA SELECCIÓN DEL USUARIO (PASAJERO / CONDUCTOR)

                    if (effectiveRole == "CONDUCTOR") {
                        val hasDriverInfo = !result.company.isNullOrBlank() && !result.routeName.isNullOrBlank()
                        if (!hasDriverInfo) {
                            _uiState.value = AuthUiState.NeedsDriverInfo(
                                email = email,
                                name = result.name,
                                role = "CONDUCTOR"
                            )
                        } else {
                            sessionManager.saveSession(
                                name = result.name,
                                role = "CONDUCTOR",
                                company = result.company,
                                route = result.routeName,
                                email = email
                            )
                            _uiState.value = AuthUiState.Success
                        }
                    } else {
                        sessionManager.saveSession(
                            name = result.name,
                            role = "PASAJERO",
                            company = null,
                            route = null,
                            email = email
                        )
                        _uiState.value = AuthUiState.Success
                    }
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // 3. COMPLETAR DATOS DE CONDUCTOR TRAS LOGIN
    fun completeDriverInfo(email: String, company: String, route: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val updated = repository.updateDriverInfo(email, company, route)
            val user = repository.getUserByEmail(email)
            val userName = user?.name ?: "Usuario"

            if (updated) {
                sessionManager.saveSession(
                    name = userName,
                    role = "CONDUCTOR",
                    company = company,
                    route = route,
                    email = email
                )
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("No se pudieron guardar los datos de conductor")
            }
        }
    }

    fun handleGoogleLoginError(errorMsg: String) {
        _uiState.value = AuthUiState.Error(errorMsg)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
