package com.morshues.morshuesandroid.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.ui.AppDestinations
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@Composable
fun LoginScreen(
    navController: NavController,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    LaunchedEffect(uiState.loginOpState) {
        if (uiState.loginOpState is LoginOpState.Success) {
            navController.navigate(AppDestinations.USER_PROFILE_ROUTE) {
                popUpTo(0)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Login",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = uiState.loginOpState is LoginOpState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = uiState.loginOpState is LoginOpState.Error
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(top = 8.dp)) {
                if (uiState.loginOpState is LoginOpState.Error) {
                    Text(
                        text = uiState.loginOpState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.loginOpState !is LoginOpState.Loading
            ) {
                Text("Login")
            }
        }

        if (uiState.loginOpState is LoginOpState.Loading) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun LoginScreenPreview() {
    MainAndroidTheme {
        LoginScreen(
            navController = rememberNavController(),
            uiState = LoginUiState(email = "test@example.com", password = "password"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode - Error")
@Composable
fun LoginScreenErrorPreview() {
    MainAndroidTheme {
        LoginScreen(
            navController = rememberNavController(),
            uiState = LoginUiState(email = "test@example.com", password = "password", LoginOpState.Error("Invalid credentials")),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
        )
    }
}