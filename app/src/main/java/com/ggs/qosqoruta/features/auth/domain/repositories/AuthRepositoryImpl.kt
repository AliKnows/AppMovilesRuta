package com.ggs.qosqoruta.features.auth.data.repository

import com.ggs.qosqoruta.core.security.PasswordSecurity
import com.ggs.qosqoruta.features.auth.data.local.AuthDao
import com.ggs.qosqoruta.features.auth.data.local.UserEntity
import com.ggs.qosqoruta.features.auth.domain.models.AuthResult
import com.ggs.qosqoruta.features.auth.domain.repositories.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authDao: AuthDao
) : IAuthRepository {

    override suspend fun registerLocalUser(
        name: String,
        email: String,
        phone: String,
        passwordRaw: String,
        role: String,
        company: String?,
        routeName: String?
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            // 1. Verificamos si el correo ya existe
            val existingUser = authDao.getUserByEmail(email)
            if (existingUser != null) {
                return@withContext AuthResult.Error("El correo ya está registrado.")
            }

            // 2. Hasheamos la contraseña de forma segura
            val hashedPassword = PasswordSecurity.hashPassword(passwordRaw)

            // 3. Guardamos en Room
            val newUser = UserEntity(
                name = name,
                email = email,
                phone = phone,
                passwordHash = hashedPassword,
                role = role,
                company = company,
                routeName = routeName
            )
            val result = authDao.insertUser(newUser)

            if (result > 0) AuthResult.Success(name = name, role = role, company = company, routeName = routeName) else AuthResult.Error("Error al guardar el usuario.")

        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Error desconocido en el registro")
        }
    }

    override suspend fun loginLocalUser(
        email: String,
        passwordRaw: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            // 1. Buscamos al usuario por correo
            val user = authDao.getUserByEmail(email)
                ?: return@withContext AuthResult.Error("Credenciales incorrectas.") // Ocultamos si el error es el correo o el pass por seguridad

            // 2. Verificamos el hash
            val isPasswordValid = PasswordSecurity.verifyPassword(passwordRaw, user.passwordHash)

            if (isPasswordValid) {
                AuthResult.Success(
                    name = user.name, 
                    role = user.role, 
                    company = user.company, 
                    routeName = user.routeName
                )
            } else {
                AuthResult.Error("Credenciales incorrectas.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Error al iniciar sesión")
        }
    }
}
