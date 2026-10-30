package com.ggs.qosqoruta.features.auth.domain.models

// Sealed class para manejar los estados lógicos del repositorio
sealed class AuthResult {
    data class Success(
        val name: String = "Usuario",
        val role: String = "PASAJERO",
        val company: String? = null,
        val routeName: String? = null
    ) : AuthResult()
    
    data class Error(val message: String) : AuthResult()
}
