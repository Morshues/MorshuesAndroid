package com.morshues.morshuesandroid.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun LoginRoute(
    navController: NavController,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()

    LoginScreen(
        navController = navController,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::submit,
    )
}