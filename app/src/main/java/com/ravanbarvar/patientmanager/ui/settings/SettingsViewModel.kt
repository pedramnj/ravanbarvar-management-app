package com.ravanbarvar.patientmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.datastore.SessionManager
import com.ravanbarvar.patientmanager.data.local.entity.AdminUserEntity
import com.ravanbarvar.patientmanager.data.repository.AuthRepository
import com.ravanbarvar.patientmanager.data.repository.ChangePasswordResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentAdmin: StateFlow<AdminUserEntity?> = authRepository.currentAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val remindersEnabled: StateFlow<Boolean> = sessionManager.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { sessionManager.setRemindersEnabled(enabled) }
    }

    private val _changePasswordState = MutableStateFlow(ChangePasswordUiState())
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    fun onCurrentPasswordChange(value: String) {
        _changePasswordState.update { it.copy(currentPassword = value, errorMessage = null, successMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _changePasswordState.update { it.copy(newPassword = value, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _changePasswordState.update { it.copy(confirmPassword = value, errorMessage = null, successMessage = null) }
    }

    fun submitChangePassword(mismatchError: String, wrongCurrentError: String, tooShortError: String, successMsg: String) {
        val state = _changePasswordState.value
        val admin = currentAdmin.value ?: return
        if (state.newPassword != state.confirmPassword) {
            _changePasswordState.update { it.copy(errorMessage = mismatchError) }
            return
        }
        viewModelScope.launch {
            _changePasswordState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = authRepository.changePassword(admin.id, state.currentPassword, state.newPassword)
            when (result) {
                is ChangePasswordResult.Success -> _changePasswordState.value = ChangePasswordUiState(successMessage = successMsg)
                is ChangePasswordResult.WrongCurrentPassword -> _changePasswordState.update {
                    it.copy(isSubmitting = false, errorMessage = wrongCurrentError)
                }
                is ChangePasswordResult.TooShort -> _changePasswordState.update {
                    it.copy(isSubmitting = false, errorMessage = tooShortError)
                }
            }
        }
    }

    fun updateFullName(fullName: String) {
        val admin = currentAdmin.value ?: return
        viewModelScope.launch { authRepository.updateFullName(admin.id, fullName) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
