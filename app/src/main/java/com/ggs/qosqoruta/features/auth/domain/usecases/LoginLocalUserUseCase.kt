package com.ggs.qosqoruta.features.auth.domain.usecases

import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.repositories.IAuthRepository

class LoginLocalUserUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(email: String, passwordRaw: String): AuthResult {
        if (email.isBlank() || passwordRaw.isBlank()) {
            return AuthResult.Error("Por favor, ingresa tu correo y contraseña.")
        }

        return repository.loginLocalUser(email, passwordRaw)
    }
}
