package com.morshues.morshuesandroid.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.model.UserDto
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginOpState {
    object Idle : LoginOpState
    object Loading : LoginOpState
    data class Success(val user: UserDto) : LoginOpState
    data class Error(val message: String) : LoginOpState
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loginOpState: LoginOpState = LoginOpState.Idle
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        loadCachedCredentials()
    }

    private fun loadCachedCredentials() {
        viewModelScope.launch {
            val email = sessionStore.cachedEmail.first() ?: ""
            val password = sessionStore.cachedPassword.first() ?: ""
            _state.update { it.copy(email = email, password = password) }
        }
    }

    fun onEmailChange(v: String) {
        _state.update { it.copy(email = v, loginOpState = LoginOpState.Idle) }
    }

    fun onPasswordChange(v: String) {
        _state.update { it.copy(password = v, loginOpState = LoginOpState.Idle) }
    }

    fun submit() {
        val (email, password) = _state.value.let { it.email to it.password }
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(loginOpState = LoginOpState.Error("Please enter your email and password")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loginOpState = LoginOpState.Loading) }
            try {
                val deviceId = sessionStore.getOrCreateDeviceId()
                val res = repo.login(email, password, deviceId)
                val expiresAt = JwtUtils.getExpirationTime(res.accessToken)
                sessionStore.saveTokens(res.accessToken, res.refreshToken, expiresAt)
                sessionStore.saveUser(res.user)
                sessionStore.saveLoginCredentials(email, password)
                _state.update { it.copy(loginOpState = LoginOpState.Success(res.user)) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Login failed"
                _state.update { it.copy(loginOpState = LoginOpState.Error(errorMessage)) }
            }
        }
    }
}