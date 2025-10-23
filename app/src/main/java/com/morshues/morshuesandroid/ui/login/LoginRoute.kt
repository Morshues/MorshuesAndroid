package com.morshues.morshuesandroid.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morshues.morshuesandroid.di.AppModule

@Composable
fun LoginRoute(
    navController: NavController,
) {
    val factory = LoginViewModelFactory(AppModule.authRepository, AppModule.sessionStore)
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val uiState by viewModel.state.collectAsState()

    LoginScreen(
        navController = navController,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::submit,
    )
}