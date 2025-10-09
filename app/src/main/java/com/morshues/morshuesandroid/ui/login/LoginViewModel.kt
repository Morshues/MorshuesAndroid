package com.morshues.morshuesandroid.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.model.UserDto
import com.morshues.morshuesandroid.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class LoginViewModel(
    application: Application,
    private val repo: AuthRepository,
    private val sessionStore: SessionStore,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

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
                sessionStore.saveTokens(res.accessToken, res.refreshToken)
                sessionStore.saveUser(res.user)
                _state.update { it.copy(loginOpState = LoginOpState.Success(res.user)) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Login failed"
                _state.update { it.copy(loginOpState = LoginOpState.Error(errorMessage)) }
            }
        }
    }
}