package com.morshues.morshuesandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.ui.AppDestinations
import com.morshues.morshuesandroid.ui.login.LoginRoute
import com.morshues.morshuesandroid.ui.main.MainViewModel
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme
import com.morshues.morshuesandroid.ui.userprofile.UserProfileRoute
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.enableEdgeToEdge
import com.morshues.morshuesandroid.ui.filesync.FileSyncRoute
import com.morshues.morshuesandroid.ui.settings.SettingsRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

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
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val showBottomBar = currentRoute != null && currentRoute != AppDestinations.LOGIN_ROUTE
                    val startDestination = mainUiState.startRoute!!

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = currentRoute == AppDestinations.FILE_SYNC_ROUTE,
                                        onClick = {
                                            navController.navigate(AppDestinations.FILE_SYNC_ROUTE) {
                                                popUpTo(startDestination) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(painterResource(R.drawable.baseline_folder_24), contentDescription = "Files") },
                                        label = { Text("Files") },
                                    )
                                    NavigationBarItem(
                                        selected = false,
                                        onClick = { },
                                        icon = { Icon(painterResource(R.drawable.baseline_menu_24), contentDescription = "Functions") },
                                        label = { Text("Functions") },
                                    )
                                    NavigationBarItem(
                                        selected = false,
                                        onClick = { },
                                        icon = { Icon(painterResource(R.drawable.round_gamepad_24), contentDescription = "Remote") },
                                        label = { Text("Remote") },
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == AppDestinations.USER_PROFILE_ROUTE,
                                        onClick = {
                                            navController.navigate(AppDestinations.USER_PROFILE_ROUTE) {
                                                popUpTo(startDestination) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(painterResource(R.drawable.round_account_circle_24), contentDescription = "Profile") },
                                        label = { Text("Profile") },
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding),
                        ) {
                            composable(AppDestinations.LOGIN_ROUTE) {
                                LoginRoute(navController = navController)
                            }
                            composable(AppDestinations.USER_PROFILE_ROUTE) {
                                UserProfileRoute(navController = navController)
                            }
                            composable(AppDestinations.FILE_SYNC_ROUTE) {
                                FileSyncRoute(navController = navController)
                            }
                            composable(AppDestinations.SETTINGS_ROUTE) {
                                SettingsRoute(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}