package com.ggs.qosqoruta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.core.preferences.ThemeManager
import com.ggs.qosqoruta.navigation.AppNavigation
import com.ggs.qosqoruta.ui.theme.QosQoRutaTheme
import com.google.android.gms.maps.MapsInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Google Maps en el hilo principal antes de renderizar la UI de Compose
        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        val sessionManager = SessionManager(this)
        val themeManager = ThemeManager(this)

        setContent {
            val themeState by themeManager.themeState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkMode = themeState ?: isSystemDark

            QosQoRutaTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        sessionManager = sessionManager,
                        themeManager = themeManager,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}
