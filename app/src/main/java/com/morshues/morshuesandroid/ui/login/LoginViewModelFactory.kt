package com.morshues.morshuesandroid.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository, sessionStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}