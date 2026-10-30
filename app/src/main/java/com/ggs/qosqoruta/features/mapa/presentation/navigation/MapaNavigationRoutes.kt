package com.ggs.qosqoruta.features.mapa.presentation.navigation

sealed class Rutas(val route: String) {
    object MapaPrincipal : Rutas("mapa_principal")
    object ModoPasajero : Rutas("modo_pasajero")
    object LineasEmpresas : Rutas("lineas_empresas")
    object Paraderos : Rutas("paraderos")
    object Tarifas : Rutas("tarifas")
    object Historial : Rutas("historial")
    object Offline : Rutas("offline")
    object Configuracion : Rutas("configuracion")
}
