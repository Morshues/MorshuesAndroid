package com.morshues.morshuesandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.di.AppModule
import com.morshues.morshuesandroid.ui.AppDestinations
import com.morshues.morshuesandroid.ui.login.LoginRoute
import com.morshues.morshuesandroid.ui.main.MainViewModel
import com.morshues.morshuesandroid.ui.main.MainViewModelFactory
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme
import com.morshues.morshuesandroid.ui.userprofile.UserProfileRoute

import androidx.activity.enableEdgeToEdge
import com.morshues.morshuesandroid.ui.filesync.FileSyncRoute

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppModule.sessionStore, AppModule.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainAndroidTheme {
                val mainUiState by mainViewModel.uiState.collectAsState()

                if (mainUiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = mainUiState.startRoute!!) {
                        composable(AppDestinations.LOGIN_ROUTE) {
                            LoginRoute(navController = navController)
                        }
                        composable(AppDestinations.USER_PROFILE_ROUTE) {
                            UserProfileRoute()
                        }
                        composable(AppDestinations.FILE_SYNC_ROUTE) {
                            FileSyncRoute(navController = navController)
                        }
                    }
                }
            }
        }
    }
}