package com.morshues.morshuesandroid.ui.userprofile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun UserProfileRoute(navController: NavController) {
    val viewModel: UserProfileViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState(initial = null)

    if (user != null) {
        UserProfileScreen(
            navController = navController,
            user = user!!,
            onLogoutClick = { viewModel.logout() }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}