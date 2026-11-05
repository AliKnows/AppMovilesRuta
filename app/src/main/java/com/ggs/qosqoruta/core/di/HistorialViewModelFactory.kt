package com.ggs.qosqoruta.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ggs.qosqoruta.features.mapa.data.local.RouteHistoryDao
import com.ggs.qosqoruta.features.mapa.presentation.screens.HistorialViewModel

class HistorialViewModelFactory(
    private val routeHistoryDao: RouteHistoryDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            return HistorialViewModel(routeHistoryDao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}