package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "brand_security_prefs"
        private const val KEY_ADMIN_PASSWORD = "key_admin_password"
        const val DEFAULT_ADMIN_PASSWORD = "MyBrandPassword123"
    }

    fun getAdminPassword(): String {
        return prefs.getString(KEY_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD) ?: DEFAULT_ADMIN_PASSWORD
    }

    fun verifyPassword(input: String): Boolean {
        val current = getAdminPassword()
        return input.trim() == current.trim()
    }

    fun updatePassword(currentInput: String, newPassword: String): Boolean {
        if (verifyPassword(currentInput) && newPassword.isNotBlank()) {
            prefs.edit().putString(KEY_ADMIN_PASSWORD, newPassword.trim()).apply()
            return true
        }
        return false
    }

    fun resetToDefault() {
        prefs.edit().putString(KEY_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD).apply()
    }
}
