package com.vitalmind.mobilewear.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "vitalmind_session"
)

class SessionManager(
    private val context: Context
) {

    companion object {
        private val ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        private val REFRESH_TOKEN =
            stringPreferencesKey("refresh_token")

        private val USER_NAME =
            stringPreferencesKey("user_name")

        private val USER_EMAIL =
            stringPreferencesKey("user_email")
    }

    val accessToken: Flow<String?> =
        context.dataStore.data.map {
            it[ACCESS_TOKEN]
        }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userName: String,
        userEmail: String
    ) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = accessToken
            it[REFRESH_TOKEN] = refreshToken
            it[USER_NAME] = userName
            it[USER_EMAIL] = userEmail
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.clear()
        }
    }
}