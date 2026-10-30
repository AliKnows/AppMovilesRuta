package com.ggs.qosqoruta.features.mapa.domain.models

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class Paradero(
    val id: String,
    val name: String,
    val snippet: String,
    val location: LatLng
)

data class RutaDetalle(
    val code: String,
    val name: String,
    val company: String,
    val itinerary: String,
    val description: String,
    val price: String,
    val frequency: String,
    val activeBusesCount: Int
)

data class BusInfo(
    val id: String,
    val name: String,
    val tag: String,
    val route: String,
    val eta: String,
    val distance: String,
    val occupancy: String,
    val occupancyColor: Color,
    val price: String,
    val frequency: String,
    val statusText: String,
    val iconColor: Color,
    val driverName: String = "Juan Yasser",
    val latLng: LatLng,
    val isFavorite: Boolean = false
)
