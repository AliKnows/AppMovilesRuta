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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sessionManager = SessionManager(this)
        val themeManager = ThemeManager(this)

        setContent {
            // Observamos el estado del tema persistente o usamos el del sistema
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
