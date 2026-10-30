package com.ggs.qosqoruta.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ggs.qosqoruta.features.auth.domain.models.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qosqoruta_prefs", Context.MODE_PRIVATE)

    fun saveSession(
        name: String,
        role: String,
        company: String? = null,
        route: String? = null,
        phone: String? = null,
        surname: String? = null,
        avatarId: Int = 0,
        email: String? = null
    ) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_name", name)
            putString("user_surname", surname ?: "")
            putString("user_role", role)
            putString("driver_company", company)
            putString("driver_route", route)
            putInt("avatar_id", avatarId)
            if (phone != null) putString("user_phone", phone)
            if (email != null) putString("user_email", email)
        }.apply()
    }

    fun updateFullProfile(name: String, surname: String, phone: String, avatarId: Int) {
        prefs.edit().apply {
            putString("user_name", name)
            putString("user_surname", surname)
            putString("user_phone", phone)
            putInt("avatar_id", avatarId)
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
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            remove("user_role")
            remove("driver_company")
            remove("driver_route")
        }.apply()
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

    fun getUserEmail(): String = prefs.getString("user_email", "guest_user") ?: "guest_user"
    fun getUserName(): String = prefs.getString("user_name", "JUAN YASSER") ?: "JUAN YASSER"
    fun getUserSurname(): String = prefs.getString("user_surname", "LIZARRAGA CONDORI") ?: "LIZARRAGA CONDORI"
    fun getFullName(): String {
        val name = getUserName()
        val surname = getUserSurname()
        return if (surname.isBlank()) name else "$name $surname"
    }
    fun getUserPhone(): String = prefs.getString("user_phone", "987654321") ?: "987654321"
    fun getAvatarId(): Int = prefs.getInt("avatar_id", 0)
    fun getDriverCompany(): String = prefs.getString("driver_company", "") ?: ""
    fun getDriverRoute(): String = prefs.getString("driver_route", "") ?: ""

    // PERSISTENCIA DE FAVORITOS ÚNICA Y PRIVADA POR CADA USUARIO (EMAIL)
    fun getFavoriteBusIds(): Set<String> {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        return prefs.getStringSet("fav_buses_$emailKey", emptySet()) ?: emptySet()
    }

    fun toggleFavoriteBus(busId: String): Set<String> {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        val current = getFavoriteBusIds().toMutableSet()
        if (current.contains(busId)) {
            current.remove(busId)
        } else {
            current.add(busId)
        }
        prefs.edit().putStringSet("fav_buses_$emailKey", current).apply()
        return current
    }

    fun removeFavoriteBus(busId: String): Set<String> {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        val current = getFavoriteBusIds().toMutableSet()
        current.remove(busId)
        prefs.edit().putStringSet("fav_buses_$emailKey", current).apply()
        return current
    }
}
