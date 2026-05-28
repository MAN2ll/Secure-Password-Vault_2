package com.securevault.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.security.Argon2Helper
import com.securevault.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LockUiState {
    object Idle : LockUiState()
    object Loading : LockUiState()
    object Success : LockUiState()
    data class Error(val message: String) : LockUiState()
}

@HiltViewModel
class LockViewModel @Inject constructor(
    private val session: SessionManager,
    private val argon2: Argon2Helper
) : ViewModel() {
    private val _state = MutableStateFlow<LockUiState>(LockUiState.Idle)
    val state: StateFlow<LockUiState> = _state

    val isSetupDone get() = session.isSetupDone()
    val isBiometricEnabled get() = session.isBiometricEnabled()

    fun setupMasterPassword(password: String, confirm: String) {
        if (password.length < 6) { _state.value = LockUiState.Error("Минимум 6 символов"); return }
        if (password != confirm) { _state.value = LockUiState.Error("Пароли не совпадают"); return }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LockUiState.Loading
            runCatching { argon2.hashPassword(password.toCharArray()) }
                .onSuccess { hash -> session.saveMasterHash(hash); session.unlock(); _state.value = LockUiState.Success }
                .onFailure { _state.value = LockUiState.Error("Ошибка создания пароля") }
        }
    }

    fun unlock(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LockUiState.Loading
            val hash = session.getMasterHash()
            if (hash != null && runCatching { argon2.verifyPassword(hash, password.toCharArray()) }.getOrDefault(false)) {
                session.unlock(); _state.value = LockUiState.Success
            } else {
                _state.value = LockUiState.Error("Неверный пароль")
            }
        }
    }

    fun unlockBiometric() { session.unlock(); _state.value = LockUiState.Success }
    fun resetError() { if (_state.value is LockUiState.Error) _state.value = LockUiState.Idle }
}
