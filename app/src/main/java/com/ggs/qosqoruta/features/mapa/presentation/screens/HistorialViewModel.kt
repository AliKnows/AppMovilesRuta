package com.ggs.qosqoruta.features.mapa.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggs.qosqoruta.features.mapa.data.local.RouteHistoryDao
import com.ggs.qosqoruta.features.mapa.data.local.RouteHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val routeHistoryDao: RouteHistoryDao
) : ViewModel() {

    // Emite automáticamente la lista ordenada cada vez que cambia la base de datos
    val historyState: StateFlow<List<RouteHistoryEntity>> = routeHistoryDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Agrega una nueva ruta al historial
    fun addRouteToHistory(origen: String, destino: String, linea: String) {
        viewModelScope.launch {
            routeHistoryDao.insertRoute(
                RouteHistoryEntity(
                    origen = origen,
                    destino = destino,
                    linea = linea
                )
            )
        }
    }

    // Elimina una ruta específica por su ID
    fun deleteRoute(id: Long) {
        viewModelScope.launch {
            routeHistoryDao.deleteRouteById(id)
        }
    }

    // Limpia todo el historial
    fun clearAll() {
        viewModelScope.launch {
            routeHistoryDao.clearAllHistory()
        }
    }
}