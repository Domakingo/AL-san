package com.zen.alchan.data.localstorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure base handler that uses EncryptedSharedPreferences
 * for sensitive data like authentication tokens.
 * 
 * This provides encryption at rest for all stored values,
 * protecting against data extraction via backup or root access.
 */
abstract class SecureSharedPreferencesHandler(
    private val context: Context,
    private val sharedPreferencesName: String
) {
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                sharedPreferencesName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            // This can happen on some devices with hardware-backed keystore issues
            Log.e("SecureSharedPrefs", "Failed to create encrypted prefs, falling back", e)
            context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        }
    }

    protected fun getData(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    protected fun setData(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    protected fun clearData() {
        sharedPreferences.edit().clear().apply()
    }
}
