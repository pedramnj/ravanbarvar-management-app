package com.ravanbarvar.patientmanager.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "ravanbarvar_session")

class SessionManager(private val context: Context) {
    private val keyAdminId = longPreferencesKey("logged_in_admin_id")
    private val keyRemindersEnabled = booleanPreferencesKey("appointment_reminders_enabled")

    val loggedInAdminId: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[keyAdminId]?.takeIf { it > 0 }
    }

    val remindersEnabled: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[keyRemindersEnabled] ?: true
    }

    suspend fun login(adminId: Long) {
        context.sessionDataStore.edit { it[keyAdminId] = adminId }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { it.remove(keyAdminId) }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[keyRemindersEnabled] = enabled }
    }
}
