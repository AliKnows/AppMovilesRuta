package com.ggs.qosqoruta.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.usecases.LoginLocalUserUseCase
import com.ggs.qosqoruta.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginLocalUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 1. FLUJO LOCAL (Room + SQLCipher)
    fun loginLocal(email: String, passwordRaw: String) {
        // Bypass para Modo Admin
        if (email == "admin@qosqoruta.com" && passwordRaw == "admin123") {
            sessionManager.saveSession(name = "Administrador", role = "PASAJERO")
            _uiState.value = AuthUiState.Success
            return
        }

        _uiState.value = AuthUiState.Loading // Bloqueo de UI
        viewModelScope.launch {
            val result = loginUseCase(email, passwordRaw)
            _uiState.value = when (result) {
                is AuthResult.Success -> {
                    sessionManager.saveSession(
                        name = result.name,
                        role = result.role,
                        company = result.company,
                        route = result.routeName
                    )
                    AuthUiState.Success
                }
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    // 2. FLUJO REMOTO (Google Credential Manager)
    fun handleGoogleLoginSuccess(idToken: String) {
        _uiState.value = AuthUiState.Loading
        sessionManager.saveSession(name = "Usuario Google", role = "PASAJERO")
        _uiState.value = AuthUiState.Success
    }

    fun handleGoogleLoginError(errorMsg: String) {
        _uiState.value = AuthUiState.Error(errorMsg)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
