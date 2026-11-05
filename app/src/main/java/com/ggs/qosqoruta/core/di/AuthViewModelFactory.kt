package com.ggs.qosqoruta.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ggs.qosqoruta.core.database.QosQoRutaDatabase
import com.ggs.qosqoruta.core.preferences.SessionManager
import com.ggs.qosqoruta.features.auth.data.repository.AuthRepositoryImpl
import com.ggs.qosqoruta.features.auth.domain.usecases.RegisterLocalUserUseCase
import com.ggs.qosqoruta.features.auth.presentation.login.LoginViewModel
import com.ggs.qosqoruta.features.auth.presentation.register.RegisterViewModel

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = QosQoRutaDatabase.getDatabase(context)
        val repository = AuthRepositoryImpl(database.authDao())
        val sessionManager = SessionManager(context)

        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(RegisterLocalUserUseCase(repository)) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository, sessionManager) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
