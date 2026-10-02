package com.example.rutacusco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rutacusco.features.explorar.UIexplorar
import com.example.rutacusco.features.home.UIhome
import com.example.rutacusco.ui.theme.RutaCuscoTheme

private const val RUTA_HOME = "home"
private const val RUTA_EXPLORAR = "explorar"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RutaCuscoTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RUTA_HOME) {
        composable(RUTA_HOME) {
            UIhome(onNavigateToExplorar = { navController.navigate(RUTA_EXPLORAR) })
        }
        composable(RUTA_EXPLORAR) {
            UIexplorar(onNavigateToHome = { navController.navigate(RUTA_HOME)})
        }
    }
}
