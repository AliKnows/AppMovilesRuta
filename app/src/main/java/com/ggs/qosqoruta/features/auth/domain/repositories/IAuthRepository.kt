package com.ggs.qosqoruta.features.auth.domain.repositories

import com.ggs.qosqoruta.features.auth.domain.models.AuthResult

interface IAuthRepository {
    // Para el registro manual
    suspend fun registerLocalUser(
        name: String, email: String, phone: String, passwordRaw: String,
        role: String, company: String?, routeName: String?
    ): AuthResult

    // Para el inicio de sesión manual
    suspend fun loginLocalUser(email: String, passwordRaw: String): AuthResult
}
