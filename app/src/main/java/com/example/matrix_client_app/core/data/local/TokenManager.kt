package com.example.matrix_client_app.core.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class TokenManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DATASTORE_NAME
    )

    companion object {
        private const val DATASTORE_NAME = "matrix_secure_prefs"
        private const val KEYSTORE_ALIAS = "MatrixClientKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_HOMESERVER_URL = stringPreferencesKey("homeserver_url")
        private val KEY_IV = stringPreferencesKey("encryption_iv")
    }

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    init {
        createKeyIfNeeded()
    }

    /**
     * Creates a new encryption key in Android Keystore if it doesn't exist
     */
    private fun createKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            Timber.d("Created new encryption key in Keystore")
        }
    }

    /**
     * Gets the encryption key from Keystore
     */
    private fun getKey(): SecretKey {
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    /**
     * Encrypts data using AES-GCM
     */
    private fun encrypt(plaintext: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return Pair(
            android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP),
            android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts data using AES-GCM
     */
    private fun decrypt(encryptedData: String, ivString: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = android.util.Base64.decode(ivString, android.util.Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        val encryptedBytes = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Saves the access token securely
     */
    suspend fun saveAccessToken(token: String) {
        try {
            val (encryptedToken, iv) = encrypt(token)
            context.dataStore.edit { preferences ->
                preferences[KEY_ACCESS_TOKEN] = encryptedToken
                preferences[KEY_IV] = iv
            }
            Timber.d("Access token saved securely")
        } catch (e: Exception) {
            Timber.e(e, "Error saving access token")
            throw e
        }
    }

    /**
     * Retrieves the access token
     */
    suspend fun getAccessToken(): String? {
        return try {
            val preferences = context.dataStore.data.first()
            val encryptedToken = preferences[KEY_ACCESS_TOKEN]
            val iv = preferences[KEY_IV]

            if (encryptedToken != null && iv != null) {
                decrypt(encryptedToken, iv)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving access token")
            null
        }
    }

    /**
     * Flow that emits the access token (useful for reactive UI)
     */
    fun getAccessTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            try {
                val encryptedToken = preferences[KEY_ACCESS_TOKEN]
                val iv = preferences[KEY_IV]

                if (encryptedToken != null && iv != null) {
                    decrypt(encryptedToken, iv)
                } else {
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in access token flow")
                null
            }
        }
    }

    /**
     * Saves the user ID (not encrypted as it's not sensitive)
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    /**
     * Gets the user ID
     */
    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[KEY_USER_ID]
    }

    /**
     * Saves the homeserver URL
     */
    suspend fun saveHomeserverUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HOMESERVER_URL] = url
        }
    }

    /**
     * Gets the homeserver URL
     */
    suspend fun getHomeserverUrl(): String? {
        return context.dataStore.data.first()[KEY_HOMESERVER_URL]
    }

    /**
     * Checks if user is logged in (has access token)
     */
    suspend fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Clears all stored credentials (logout)
     */
    suspend fun clearAll() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            Timber.d("All credentials cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing credentials")
            throw e
        }
    }

    /**
     * Clears only the access token (for token refresh scenarios)
     */
    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_IV)
        }
    }
}