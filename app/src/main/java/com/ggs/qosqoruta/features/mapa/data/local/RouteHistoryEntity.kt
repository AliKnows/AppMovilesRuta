package com.ggs.qosqoruta.features.mapa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_rutas")
data class RouteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val origen: String,
    val destino: String,
    val linea: String,
    val timestamp: Long = System.currentTimeMillis()
)