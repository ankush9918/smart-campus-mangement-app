package com.vvit.smartcollege.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

object ThemeState {
    private const val PREFS_NAME = "vvit_theme_prefs"
    private const val KEY_DARK_MODE = "is_dark_mode"

    private var prefs: SharedPreferences? = null

    val isDarkMode = mutableStateOf(false)

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            isDarkMode.value = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
        }
    }

    fun toggleDarkMode(context: Context? = null) {
        setDarkMode(!isDarkMode.value, context)
    }

    fun setDarkMode(enabled: Boolean, context: Context? = null) {
        isDarkMode.value = enabled
        if (prefs == null && context != null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
    }
}
