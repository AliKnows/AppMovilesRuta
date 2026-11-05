package com.ggs.qosqoruta.features.auth.domain.usecases

import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.repositories.IAuthRepository

class RegisterLocalUserUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(
        name: String, email: String, phone: String, passwordRaw: String,
        role: String, company: String?, routeName: String?
    ): AuthResult {
        // Validación de reglas de negocio básicas
        if (name.isBlank() || email.isBlank() || phone.isBlank() || passwordRaw.isBlank()) {
            return AuthResult.Error("Todos los campos principales son obligatorios.")
        }
        if (passwordRaw.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres.")
        }
        if (role == "CONDUCTOR" && (company.isNullOrBlank() || routeName.isNullOrBlank())) {
            return AuthResult.Error("Los conductores deben indicar empresa y ruta.")
        }

        return repository.registerLocalUser(name, email, phone, passwordRaw, role, company, routeName)
    }
}
