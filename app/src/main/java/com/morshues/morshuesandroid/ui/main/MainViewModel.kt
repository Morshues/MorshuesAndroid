package com.morshues.morshuesandroid.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.ui.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val startRoute: String? = null
)

class MainViewModel(
    application: Application,
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val deviceId = sessionStore.getOrCreateDeviceId()
            val refreshToken = sessionStore.refreshToken.first()

            if (refreshToken == null) {
                _uiState.update { it.copy(isLoading = false, startRoute = AppDestinations.LOGIN_ROUTE) }
                return@launch
            }
            try {
                val response = authRepository.refresh(refreshToken, deviceId)
                sessionStore.saveTokens(response.accessToken, response.refreshToken)
                _uiState.update { it.copy(isLoading = false, startRoute = AppDestinations.USER_PROFILE_ROUTE) }
            } catch (e: Exception) {
                // Refresh token failed, clear session and go to login
                sessionStore.clear()
                _uiState.update { it.copy(isLoading = false, startRoute = AppDestinations.LOGIN_ROUTE) }
            }
        }
    }
}