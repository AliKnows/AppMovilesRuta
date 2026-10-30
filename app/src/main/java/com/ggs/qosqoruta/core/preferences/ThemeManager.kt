package com.ggs.qosqoruta.core.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qosqoruta_theme_prefs", Context.MODE_PRIVATE)

    // Null significa que sigue el tema por defecto del sistema
    private val _themeState = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    
    val themeState: StateFlow<Boolean?> = _themeState.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        _themeState.value = isDark
    }
}
