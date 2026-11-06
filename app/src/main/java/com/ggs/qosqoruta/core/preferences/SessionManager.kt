package com.ggs.qosqoruta.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ggs.qosqoruta.features.auth.domain.models.UserRole

data class SearchHistoryItem(
    val id: String,
    val title: String,
    val lineCode: String,
    val timeAgoText: String
)

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
        email: String? = null,
        avatarUri: String? = null
    ) {
        val targetEmail = email ?: getUserEmail()
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
            if (avatarUri != null) putString("user_avatar_uri_$targetEmail", avatarUri)
        }.apply()
    }

    fun updateFullProfile(name: String, surname: String, phone: String, avatarId: Int, avatarUri: String? = null) {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        prefs.edit().apply {
            putString("user_name", name)
            putString("user_surname", surname)
            putString("user_phone", phone)
            putInt("avatar_id", avatarId)
            if (avatarUri != null) putString("user_avatar_uri_$emailKey", avatarUri)
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

    // PERSISTENCIA DE URI FOTO DE PERFIL (GALERÍA O FOTO DE GOOGLE)
    fun getAvatarUri(): String? {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        return prefs.getString("user_avatar_uri_$emailKey", null)
    }

    fun setAvatarUri(uriString: String?) {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        prefs.edit().apply {
            if (uriString != null) {
                putString("user_avatar_uri_$emailKey", uriString)
            } else {
                remove("user_avatar_uri_$emailKey")
            }
        }.apply()
    }

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

    // PERSISTENCIA DE HISTORIAL DE BÚSQUEDAS
    fun getSearchHistory(): List<SearchHistoryItem> {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        val rawSet = prefs.getStringSet("search_history_$emailKey", null)
        if (rawSet == null || rawSet.isEmpty()) {
            return listOf(
                SearchHistoryItem("H1", "Plaza de Armas ➔ Paradero Final", "Línea: RTI-07", "Hace 9 minutos"),
                SearchHistoryItem("H2", "UNSAAC ➔ Mercado San Pedro", "Línea: RTU-01", "Hace 25 minutos")
            )
        }
        return rawSet.mapNotNull { itemStr ->
            val parts = itemStr.split("|||")
            if (parts.size >= 4) {
                SearchHistoryItem(parts[0], parts[1], parts[2], parts[3])
            } else null
        }
    }

    fun addSearchHistory(title: String, lineCode: String) {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        val current = getSearchHistory().toMutableList()
        val newItem = SearchHistoryItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            lineCode = lineCode,
            timeAgoText = "Hace un momento"
        )
        current.add(0, newItem)
        val serializedSet = current.take(10).map { "${it.id}|||${it.title}|||${it.lineCode}|||${it.timeAgoText}" }.toSet()
        prefs.edit().putStringSet("search_history_$emailKey", serializedSet).apply()
    }

    fun removeSearchHistoryItem(id: String) {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        val current = getSearchHistory().filterNot { it.id == id }
        val serializedSet = current.map { "${it.id}|||${it.title}|||${it.lineCode}|||${it.timeAgoText}" }.toSet()
        prefs.edit().putStringSet("search_history_$emailKey", serializedSet).apply()
    }

    fun clearSearchHistory() {
        val emailKey = getUserEmail().ifBlank { "guest_user" }
        prefs.edit().remove("search_history_$emailKey").apply()
    }
}
