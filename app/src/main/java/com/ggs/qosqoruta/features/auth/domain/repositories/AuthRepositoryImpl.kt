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
            val existingUser = authDao.getUserByEmail(email)
            if (existingUser != null) {
                return@withContext AuthResult.Error("El correo ya está registrado.")
            }

            val hashedPassword = PasswordSecurity.hashPassword(passwordRaw)

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
            val user = authDao.getUserByEmail(email)
                ?: return@withContext AuthResult.Error("Credenciales incorrectas.")

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

    override suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        try {
            authDao.getUserByEmail(email)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loginOrRegisterGoogleUser(
        email: String,
        name: String,
        role: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val existingUser = authDao.getUserByEmail(email)
            if (existingUser != null) {
                AuthResult.Success(
                    name = existingUser.name,
                    role = existingUser.role,
                    company = existingUser.company,
                    routeName = existingUser.routeName
                )
            } else {
                val newGoogleUser = UserEntity(
                    name = name,
                    email = email,
                    phone = "",
                    passwordHash = "",
                    role = role,
                    company = null,
                    routeName = null
                )
                authDao.insertUser(newGoogleUser)
                AuthResult.Success(
                    name = name,
                    role = role,
                    company = null,
                    routeName = null
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Error al autenticar con Google")
        }
    }

    override suspend fun updateDriverInfo(
        email: String,
        company: String,
        routeName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val existingUser = authDao.getUserByEmail(email)
            if (existingUser != null) {
                val updatedUser = existingUser.copy(
                    role = "CONDUCTOR",
                    company = company,
                    routeName = routeName
                )
                authDao.updateUser(updatedUser)
                true
            } else {
                val newUser = UserEntity(
                    name = "Conductor",
                    email = email,
                    phone = "",
                    passwordHash = "",
                    role = "CONDUCTOR",
                    company = company,
                    routeName = routeName
                )
                authDao.insertUser(newUser)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePassword(
        email: String,
        oldPasswordRaw: String,
        newPasswordRaw: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val user = authDao.getUserByEmail(email)
                ?: return@withContext AuthResult.Error("Usuario no encontrado en la base de datos")

            if (user.passwordHash.isNotBlank()) {
                val isOldPasswordValid = PasswordSecurity.verifyPassword(oldPasswordRaw, user.passwordHash)
                if (!isOldPasswordValid) {
                    return@withContext AuthResult.Error("La contraseña actual es incorrecta")
                }
            }

            val newHash = PasswordSecurity.hashPassword(newPasswordRaw)
            val updatedUser = user.copy(passwordHash = newHash)
            authDao.updateUser(updatedUser)

            AuthResult.Success(name = user.name, role = user.role, company = user.company, routeName = user.routeName)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Error al actualizar contraseña")
        }
    }

    override suspend fun updateFullProfile(
        email: String,
        name: String,
        phone: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = authDao.getUserByEmail(email)
            if (user != null) {
                val updatedUser = user.copy(name = name, phone = phone)
                authDao.updateUser(updatedUser)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
