package com.olx.ps.vault.api

import com.bettercloud.vault.VaultConfig
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI

class Logical(private val okHttpClient: OkHttpClient, private val vaultConfig: VaultConfig) {
    private val vaultUri = URI.create(vaultConfig.address)

    fun asyncRead(path: String, responseCallback: Callback) {
        val url = HttpUrl.Builder()
            .scheme(vaultUri.scheme)
            .host(vaultUri.host)
            .let { if (vaultUri.port != -1) it.port(vaultUri.port) else it }
            .addPathSegment(VAULT_VERSION)
            .addPathSegment(path)
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader(X_VAULT_TOKEN, vaultConfig.token)
            .get()
            .build()
        okHttpClient.newCall(request).enqueue(responseCallback)
    }
}

private const val VAULT_VERSION = "v1"
private const val X_VAULT_TOKEN = "X-Vault-Token"