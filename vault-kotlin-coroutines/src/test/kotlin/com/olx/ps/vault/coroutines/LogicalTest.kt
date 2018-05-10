package com.olx.ps.vault.coroutines

import arrow.core.getOrElse
import com.olx.ps.vault.api.Logical
import com.olx.ps.vault.common.test.SECRET_RESPONSE
import com.olx.ps.vault.config
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit.MILLISECONDS

class LogicalTest {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, MILLISECONDS)
        .readTimeout(10, MILLISECONDS)
        .build()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `suspendable read`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(SECRET_RESPONSE))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"

        val logical = Logical(
            httpClient = okHttpClient,
            jsonParser = moshi,
            config = config { address(baseUrl) }
        )

        val response = logical.read("secret").getOrElse { throw it }

        response.body()?.string() shouldEqual SECRET_RESPONSE
    }

    @Test
    fun `exception in read if something happens`() {
        runBlocking {
            val server = MockWebServer()
            server.start()
            val baseUrl = "http://127.0.0.1:${server.port}"

            val logical = Logical(
                httpClient = okHttpClient,
                jsonParser = moshi,
                config = config { address(baseUrl) }
            )

            shouldThrow<SocketTimeoutException> {
                logical.read("secret")
                    .getOrElse { throw it }
                    .let { fail("Expected SocketTimeoutException but received $it") }
            }
        }
    }

    @Test
    fun `suspendable write`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"

        val logical = Logical(
            httpClient = okHttpClient,
            jsonParser = moshi,
            config = config { address(baseUrl) }
        )

        val expectedRequest = mapOf("key" to "value")
        val response = logical.write("secret", expectedRequest).getOrElse { throw it }

        response.code() shouldEqual 204
        val jsonRequest = server.takeRequest()?.run {
            moshi.adapter<Map<String, Any>>(Map::class.java).fromJson(body)
        }
        jsonRequest shouldEqual expectedRequest
    }

    @Test
    fun `exception in write if something happens`() {
        runBlocking {
            val server = MockWebServer()
            server.start()
            val baseUrl = "http://127.0.0.1:${server.port}"

            val logical = Logical(
                httpClient = okHttpClient,
                jsonParser = moshi,
                config = config { address(baseUrl) }
            )

            shouldThrow<SocketTimeoutException> {
                logical.write("secret", mapOf("key" to "value"))
                    .getOrElse { throw it }
                    .let { fail("Expected SocketTimeoutException but received $it") }
            }
        }
    }
}