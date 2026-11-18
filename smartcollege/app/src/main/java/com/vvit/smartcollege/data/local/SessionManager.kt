package com.vvit.smartcollege.data.local

import android.content.Context
import android.content.SharedPreferences
import com.vvit.smartcollege.data.model.User
import com.vvit.smartcollege.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "vvit_smartcollege_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_IDENTIFIER = "user_identifier"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_ACCESS_TOKEN = "access_token"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }

        fun get(): SessionManager? = instance
    }

    fun saveSession(user: User, accessToken: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role.name)
            putString(KEY_USER_IDENTIFIER, user.identifier)
            putString(KEY_USER_AVATAR, user.avatarUrl)
            if (!accessToken.isNullOrBlank()) {
                val encryptedToken = SecureTokenStorage.encryptToken(accessToken)
                putString(KEY_ACCESS_TOKEN, encryptedToken)
            }
            apply()
        }
    }

    fun saveAccessToken(token: String) {
        val encryptedToken = SecureTokenStorage.encryptToken(token)
        prefs.edit().putString(KEY_ACCESS_TOKEN, encryptedToken).apply()
    }

    fun getAccessToken(): String? {
        val raw = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        return SecureTokenStorage.decryptToken(raw)
    }

    fun getSession(): User? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        val identifier = prefs.getString(KEY_USER_IDENTIFIER, "") ?: ""
        val avatarUrl = prefs.getString(KEY_USER_AVATAR, null)

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            return null
        }

        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            avatarUrl = avatarUrl,
            identifier = identifier
        )
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
