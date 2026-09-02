package com.ravanbarvar.patientmanager.data.repository

import com.ravanbarvar.patientmanager.data.datastore.SessionManager
import com.ravanbarvar.patientmanager.data.local.dao.AdminDao
import com.ravanbarvar.patientmanager.data.local.entity.AdminUserEntity
import com.ravanbarvar.patientmanager.util.PasswordHasher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
}

sealed class ChangePasswordResult {
    data object Success : ChangePasswordResult()
    data object WrongCurrentPassword : ChangePasswordResult()
    data object TooShort : ChangePasswordResult()
}

class AuthRepository(
    private val adminDao: AdminDao,
    private val sessionManager: SessionManager
) {
    val isLoggedIn: Flow<Boolean> = sessionManager.loggedInAdminId.map { it != null }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAdmin: Flow<AdminUserEntity?> = sessionManager.loggedInAdminId.flatMapLatest { id ->
        if (id == null) flowOf(null) else adminDao.observeById(id)
    }

    suspend fun login(username: String, password: String): LoginResult {
        val admin = adminDao.getByUsername(username.trim()) ?: return LoginResult.InvalidCredentials
        return if (PasswordHasher.verify(password, admin.salt, admin.passwordHash)) {
            sessionManager.login(admin.id)
            LoginResult.Success
        } else {
            LoginResult.InvalidCredentials
        }
    }

    suspend fun logout() {
        sessionManager.logout()
    }

    suspend fun changePassword(adminId: Long, currentPassword: String, newPassword: String): ChangePasswordResult {
        val admin = adminDao.getById(adminId) ?: return ChangePasswordResult.WrongCurrentPassword
        if (!PasswordHasher.verify(currentPassword, admin.salt, admin.passwordHash)) {
            return ChangePasswordResult.WrongCurrentPassword
        }
        if (newPassword.length < 4) return ChangePasswordResult.TooShort
        val newSalt = PasswordHasher.generateSalt()
        val newHash = PasswordHasher.hash(newPassword, newSalt)
        adminDao.update(admin.copy(passwordHash = newHash, salt = newSalt))
        return ChangePasswordResult.Success
    }

    suspend fun updateFullName(adminId: Long, fullName: String) {
        val admin = adminDao.getById(adminId) ?: return
        adminDao.update(admin.copy(fullName = fullName))
    }
}
