package com.ggs.qosqoruta.features.auth.domain.repositories

import com.ggs.qosqoruta.features.auth.data.local.UserEntity
import com.ggs.qosqoruta.features.auth.domain.models.AuthResult

interface IAuthRepository {
    suspend fun registerLocalUser(
        name: String, email: String, phone: String, passwordRaw: String,
        role: String, company: String?, routeName: String?
    ): AuthResult

    suspend fun loginLocalUser(email: String, passwordRaw: String): AuthResult

    suspend fun getUserByEmail(email: String): UserEntity?

    suspend fun loginOrRegisterGoogleUser(email: String, name: String, role: String): AuthResult

    suspend fun updateDriverInfo(email: String, company: String, routeName: String): Boolean

    suspend fun updatePassword(email: String, oldPasswordRaw: String, newPasswordRaw: String): AuthResult

    suspend fun updateFullProfile(email: String, name: String, phone: String): Boolean
}
