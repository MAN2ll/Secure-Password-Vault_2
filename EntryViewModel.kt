package com.securevault.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "sv_prefs"
        private const val KEY_MASTER_HASH = "master_hash"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_SETUP = "setup_done"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context, PREFS_NAME, masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Volatile private var unlocked = false

    fun isSetupDone() = prefs.getBoolean(KEY_SETUP, false)
    fun getMasterHash(): String? = prefs.getString(KEY_MASTER_HASH, null)
    fun saveMasterHash(hash: String) {
        prefs.edit().putString(KEY_MASTER_HASH, hash).putBoolean(KEY_SETUP, true).apply()
    }
    fun unlock() { unlocked = true }
    fun lock() { unlocked = false }
    fun isUnlocked() = unlocked
    fun isBiometricEnabled() = prefs.getBoolean(KEY_BIOMETRIC, false)
    fun setBiometricEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_BIOMETRIC, v).apply() }
    fun reset() { prefs.edit().clear().apply(); unlocked = false }
}
