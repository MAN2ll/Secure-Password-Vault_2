package com.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onBiometricRequest: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    val isSetup = viewModel.isSetupDone

    LaunchedEffect(state) { if (state is LockUiState.Success) onUnlocked() }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Security, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Secure Vault", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                if (isSetup) "Введите мастер-пароль" else "Создайте мастер-пароль",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it; viewModel.resetError() },
                label = { Text("Мастер-пароль") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton({ showPwd = !showPwd }) {
                        Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            if (!isSetup) {
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it; viewModel.resetError() },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }

            if (state is LockUiState.Error)
                Text((state as LockUiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)

            Button(
                onClick = { if (isSetup) viewModel.unlock(password) else viewModel.setupMasterPassword(password, confirm) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = state !is LockUiState.Loading
            ) {
                if (state is LockUiState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text(if (isSetup) "Войти" else "Создать хранилище")
            }

            if (isSetup && viewModel.isBiometricEnabled) {
                OutlinedButton(onClick = onBiometricRequest, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Fingerprint, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Войти по биометрии")
                }
            }

            if (!isSetup) {
                Text(
                    "Защита: Argon2id + AES-256-GCM + Android Keystore",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                )
            }
        }
    }
}
