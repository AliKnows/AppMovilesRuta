package com.ggs.qosqoruta.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.usecases.RegisterLocalUserUseCase
import com.ggs.qosqoruta.features.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterLocalUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(
        name: String, email: String, phone: String, passwordRaw: String,
        role: String, company: String?, routeName: String?
    ) {
        _uiState.value = AuthUiState.Loading // Bloqueamos la UI

        viewModelScope.launch {
            val result = registerUseCase(name, email, phone, passwordRaw, role, company, routeName)
            _uiState.value = when (result) {
                is AuthResult.Success -> AuthUiState.Success
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
