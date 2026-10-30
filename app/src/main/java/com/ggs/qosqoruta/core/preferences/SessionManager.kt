package com.ggs.qosqoruta.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ggs.qosqoruta.features.auth.domain.models.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qosqoruta_prefs", Context.MODE_PRIVATE)

    fun saveSession(name: String, role: String, company: String? = null, route: String? = null) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_name", name)
            putString("user_role", role)
            putString("driver_company", company)
            putString("driver_route", route)
        }.apply()
    }

    fun updateRoleToDriver(company: String, route: String) {
        prefs.edit().apply {
            putString("user_role", UserRole.CONDUCTOR.name)
            putString("driver_company", company)
            putString("driver_route", route)
        }.apply()
    }

    fun updateRoleToPassenger() {
        prefs.edit().apply {
            putString("user_role", UserRole.PASAJERO.name)
            remove("driver_company")
            remove("driver_route")
        }.apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getUserRole(): UserRole {
        val roleStr = prefs.getString("user_role", UserRole.PASAJERO.name) ?: UserRole.PASAJERO.name
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.PASAJERO
        }
    }

    fun getUserName(): String = prefs.getString("user_name", "Usuario") ?: "Usuario"
    fun getDriverCompany(): String = prefs.getString("driver_company", "") ?: ""
    fun getDriverRoute(): String = prefs.getString("driver_route", "") ?: ""
}
