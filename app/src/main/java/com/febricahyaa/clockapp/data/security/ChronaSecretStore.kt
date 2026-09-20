/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.febricahyaa.clockapp.data.chronaSecretsDataStore
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ChronaSecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun writeAuthToken(token: String) {
        val encrypted = encrypt(token.toByteArray(Charsets.UTF_8))
        context.chronaSecretsDataStore.edit { values ->
            values[KEY_AUTH_TOKEN] = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }
    }

    suspend fun readAuthToken(): String? {
        val encoded = context.chronaSecretsDataStore.data.first()[KEY_AUTH_TOKEN] ?: return null
        return runCatching {
            String(decrypt(Base64.decode(encoded, Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrElse { error ->
            if (error is KeyPermanentlyInvalidatedException) {
                clearAuthToken()
            }
            null
        }
    }

    suspend fun clearAuthToken() {
        context.chronaSecretsDataStore.edit { it.remove(KEY_AUTH_TOKEN) }
    }

    private fun encrypt(plainText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainText)
        val payload = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(ciphertext, 0, payload, iv.size, ciphertext.size)
        return payload
    }

    private fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size > IV_SIZE) { "Invalid Chrona secret payload" }
        val iv = payload.copyOfRange(0, IV_SIZE)
        val ciphertext = payload.copyOfRange(IV_SIZE, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "chrona_secret_aes_gcm_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
}
