package com.example.matrix_client_app.core.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64.NO_WRAP
import android.util.Base64.decode
import android.util.Base64.encodeToString
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

    private fun getKey(): SecretKey {
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    private fun encrypt(plaintext: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return Pair(
            encodeToString(encryptedBytes, NO_WRAP),
            encodeToString(iv, NO_WRAP)
        )
    }

    private fun decrypt(encryptedData: String, ivString: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = decode(ivString, NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        val encryptedBytes = decode(encryptedData, NO_WRAP)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

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

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[KEY_USER_ID]
    }

    suspend fun saveHomeserverUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HOMESERVER_URL] = url
        }
    }

    suspend fun getHomeserverUrl(): String? {
        return context.dataStore.data.first()[KEY_HOMESERVER_URL]
    }

    suspend fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

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

    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_IV)
        }
    }
}