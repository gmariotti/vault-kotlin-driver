package com.olx.ps.vault.api

import com.olx.ps.vault.config
import com.olx.ps.vault.coroutines.read
import com.olx.ps.vault.coroutines.write
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.kotlintest.matchers.shouldEqual
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import org.junit.ClassRule
import org.junit.Test
import org.testcontainers.vault.VaultContainer
import java.util.UUID
import java.util.concurrent.TimeUnit.MILLISECONDS

class LogicalIntegrationTest {

    companion object {
        private val vaultToken = UUID.randomUUID().toString()
        @ClassRule @JvmField val container: KVaultContainer = KVaultContainer("vault:0.9.6")
            .withVaultPort(8200)
            .withVaultToken(vaultToken)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(100, MILLISECONDS)
        .connectTimeout(100, MILLISECONDS)
        .build()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `write secret to vault and read it using callback`() = runBlocking {
        val config = config {
            address(container.url(8200))
            token(vaultToken)
        }
        val logical = Logical(
            httpClient = okHttpClient,
            jsonParser = moshi,
            config = config
        )
        val toWrite = mapOf("key" to "value", "key_1" to 1.0)

        logical.write("secret/foo", toWrite).fold(
            fa = { throw it },
            fb = { it.code() shouldEqual 204 }
        )

        logical.read("secret/foo").fold(
            fa = { throw it },
            fb = {
                val body = moshi.adapter<Map<String, Any>>(Map::class.java)
                    .fromJson(it.body()?.string() ?: "{}")
                    ?: emptyMap()
                body["data"] shouldEqual toWrite
            }
        )
    }
}

class KVaultContainer(dockerImageName: String) : VaultContainer<KVaultContainer>(dockerImageName) {
    fun url(originalPort: Int) = "http://${this.containerIpAddress}:${this.getMappedPort(originalPort)}"
}