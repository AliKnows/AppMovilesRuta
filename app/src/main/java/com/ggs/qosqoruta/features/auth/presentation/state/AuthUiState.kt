package com.ggs.qosqoruta.features.auth.presentation.state

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class NeedsDriverInfo(
        val email: String,
        val name: String,
        val role: String
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
