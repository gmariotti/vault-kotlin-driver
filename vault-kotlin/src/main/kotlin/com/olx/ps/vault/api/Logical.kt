package com.olx.ps.vault.api

import com.bettercloud.vault.VaultConfig
import com.squareup.moshi.Moshi
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URI

public class Logical(
    private val httpClient: OkHttpClient,
    private val jsonParser: Moshi,
    private val config: VaultConfig
) {
    private val vaultUri = URI.create(config.address)

    public fun asyncRead(path: String, responseCallback: Callback): Unit {
        val url = buildVaultUrl(path)
        val request = Request.Builder()
            .url(url)
            .addHeader(X_VAULT_TOKEN, config.token)
            .get()
            .build()
        httpClient.newCall(request).enqueue(responseCallback)
    }

    public fun asyncWrite(path: String, values: Map<String, Any>, responseCallback: Callback): Unit {
        val url = buildVaultUrl(path)
        val jsonAdapter = jsonParser.adapter<Map<String, Any>>(Map::class.java)
        val request = Request.Builder()
            .url(url)
            .addHeader(X_VAULT_TOKEN, config.token)
            .post(RequestBody.create(MediaType.parse("application/json"), jsonAdapter.toJson(values)))
            .build()
        httpClient.newCall(request).enqueue(responseCallback)
    }

    private fun buildVaultUrl(path: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(vaultUri.scheme)
            .host(vaultUri.host)
            .let { if (vaultUri.port != -1) it.port(vaultUri.port) else it }
            .addPathSegment(VAULT_VERSION)
            .addPathSegment(path)
            .build()
    }
}

private const val VAULT_VERSION = "v1"
private const val X_VAULT_TOKEN = "X-Vault-Token"