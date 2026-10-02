package com.example.producthunt.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.AEADBadTagException

/**
 * Guarda o developer token do Product Hunt de forma criptografada
 * (AES-256-GCM, chave mestra no Android Keystore), no mesmo padrão
 * usado nos outros apps para armazenar tokens/API keys sensíveis.
 *
 * Se o armazenamento criptografado ficar corrompido — por exemplo depois de
 * reinstalar o app várias vezes durante testes, trocar entre build debug/release,
 * ou o Keystore do aparelho/emulador ser resetado — a leitura falha com
 * AEADBadTagException porque a chave não bate mais com o que foi salvo antes.
 * Nesse caso, em vez de travar o app, apagamos o armazenamento corrompido e a
 * chave mestra antiga e recriamos do zero (o usuário só precisa informar o
 * token de novo).
 */
class TokenStore(private val context: Context) {

    private val prefs: SharedPreferences = buildPrefs(allowReset = true)

    private fun buildPrefs(allowReset: Boolean): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val candidate = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            // Força a leitura agora pra detectar corrupção já na inicialização,
            // em vez de só explodir depois no meio do uso do app.
            candidate.getString(KEY_TOKEN, null)
            candidate
        } catch (e: Exception) {
            if (allowReset && isCorruptionError(e)) {
                resetCorruptedState()
                buildPrefs(allowReset = false) // tenta reconstruir do zero só uma vez
            } else {
                throw e
            }
        }
    }

    private fun isCorruptionError(e: Exception): Boolean {
        return e is AEADBadTagException ||
            e is GeneralSecurityException ||
            e.cause is AEADBadTagException
    }

    private fun resetCorruptedState() {
        // Limpa os dados criptografados corrompidos
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        context.deleteSharedPreferences(PREFS_NAME)
        // Remove a chave mestra antiga do Android Keystore pra forçar a geração de uma nova
        runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token.trim()).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "product_hunt_secure_prefs"
        private const val KEY_TOKEN = "ph_developer_token"
    }
}
