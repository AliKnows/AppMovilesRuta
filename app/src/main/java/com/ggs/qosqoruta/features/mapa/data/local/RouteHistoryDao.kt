package com.ggs.qosqoruta.features.mapa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteHistoryDao {
    @Query("SELECT * FROM historial_rutas ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RouteHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteHistoryEntity): Long // <-- Retorna Long en lugar de Unit

    @Query("DELETE FROM historial_rutas WHERE id = :routeId")
    suspend fun deleteRouteById(routeId: Long): Int // <-- Retorna Int (filas eliminadas)

    @Query("DELETE FROM historial_rutas")
    suspend fun clearAllHistory(): Int // <-- Retorna Int (filas eliminadas)
}