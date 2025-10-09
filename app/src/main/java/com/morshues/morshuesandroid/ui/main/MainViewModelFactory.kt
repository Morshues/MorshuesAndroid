package com.morshues.morshuesandroid.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository

class MainViewModelFactory(
    private val application: Application,
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, sessionStore, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}