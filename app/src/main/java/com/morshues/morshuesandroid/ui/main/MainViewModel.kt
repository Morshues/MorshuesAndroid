package com.morshues.morshuesandroid.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.ui.AppDestinations
import com.morshues.morshuesandroid.utils.JwtUtils
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
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository
) : ViewModel() {

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
                val expiresAt = JwtUtils.getExpirationTime(response.accessToken)
                sessionStore.saveTokens(response.accessToken, response.refreshToken, expiresAt)
                _uiState.update { it.copy(isLoading = false, startRoute = AppDestinations.FILE_SYNC_ROUTE) }
            } catch (e: Exception) {
                Log.i(TAG, "Refresh token failed: (${e.message})")
                sessionStore.clear()
                _uiState.update { it.copy(isLoading = false, startRoute = AppDestinations.LOGIN_ROUTE) }
            }
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}