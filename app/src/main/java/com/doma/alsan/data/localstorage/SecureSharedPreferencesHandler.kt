package com.doma.alsan.data.localstorage

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.security.GeneralSecurityException

/**
 * Secure base handler that uses Jetpack DataStore with Google Tink encryption.
 * 
 * Architecture:
 * - Storage: Jetpack DataStore (async, coroutine-based)
 * - Encryption: Google Tink AEAD (AES256-GCM)
 * - Key Management: Android Keystore (hardware-backed when available)
 * 
 * Security Policy:
 * - If decryption fails, data is considered compromised and cleared
 */
abstract class SecureSharedPreferencesHandler(
    private val context: Context,
    private val sharedPreferencesName: String
) {
    companion object {
        private const val TAG = "SecureDataStore"
        private const val KEYSTORE_ALIAS = "alsan_tink_keyset"
        private const val KEYSTORE_PREF_NAME = "alsan_tink_keystore"
        private const val TINK_KEYSET_NAME = "alsan_master_keyset"
    }

    // DataStore instance - created lazily per preferences name
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "${sharedPreferencesName}_secure"
    )

    // Tink AEAD primitive for encryption/decryption
    private val aead: Aead by lazy {
        try {
            AeadConfig.register()
            val keysetManager = AndroidKeysetManager.Builder()
                .withSharedPref(context, TINK_KEYSET_NAME, KEYSTORE_PREF_NAME)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://$KEYSTORE_ALIAS")
                .build()
            keysetManager.keysetHandle.getPrimitive(Aead::class.java)
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Critical: Failed to initialize Tink AEAD", e)
            throw SecurityException("Cannot initialize secure storage. Please reinstall the app.", e)
        }
    }

    /**
     * Encrypts a string value using Tink AEAD.
     * @throws SecurityException if encryption fails
     */
    private fun encrypt(plaintext: String): String {
        return try {
            val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
            val ciphertext = aead.encrypt(plaintextBytes, null)
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Encryption failed", e)
            throw SecurityException("Failed to encrypt data", e)
        }
    }

    /**
     * Decrypts a string value using Tink AEAD.
     * @return decrypted string, or null if decryption fails (data corrupted/key invalid)
     */
    private fun decrypt(ciphertext: String): String? {
        return try {
            val ciphertextBytes = Base64.decode(ciphertext, Base64.NO_WRAP)
            val plaintext = aead.decrypt(ciphertextBytes, null)
            String(plaintext, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            // Decryption failed - key was invalidated or data is corrupted
            // This is unrecoverable, the data must be considered lost
            Log.e(TAG, "Decryption failed - data is corrupted or key was invalidated", e)
            null
        } catch (e: IllegalArgumentException) {
            // Base64 decoding failed - data is corrupted
            Log.e(TAG, "Base64 decoding failed - data is corrupted", e)
            null
        }
    }

    protected fun getData(key: String): String? {
        return runBlocking {
            try {
                val encrypted = context.dataStore.data.map { prefs ->
                    prefs[stringPreferencesKey(key)]
                }.first()
                
                if (encrypted == null) {
                    return@runBlocking null
                }
                
                val decrypted = decrypt(encrypted)
                if (decrypted == null) {
                    // Decryption failed - clear the corrupted entry
                    Log.w(TAG, "Clearing corrupted data for key: $key")
                    context.dataStore.edit { prefs ->
                        prefs.remove(stringPreferencesKey(key))
                    }
                }
                decrypted
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get data for key: $key", e)
                null
            }
        }
    }

    protected fun setData(key: String, value: String?) {
        runBlocking {
            try {
                context.dataStore.edit { prefs ->
                    if (value != null) {
                        prefs[stringPreferencesKey(key)] = encrypt(value)
                    } else {
                        prefs.remove(stringPreferencesKey(key))
                    }
                }
            } catch (e: SecurityException) {
                // Encryption failed - this is a critical error
                Log.e(TAG, "Critical: Failed to encrypt data for key: $key", e)
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set data for key: $key", e)
            }
        }
    }

    protected fun clearData() {
        runBlocking {
            try {
                context.dataStore.edit { prefs ->
                    prefs.clear()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear data", e)
            }
        }
    }
}
