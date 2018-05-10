package com.olx.ps.vault.api

import com.olx.ps.vault.common.test.SECRET_RESPONSE
import com.olx.ps.vault.config
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.kotlintest.matchers.shouldEqual
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import java.io.IOException
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
    fun `use callback to read from vault`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(SECRET_RESPONSE))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"
        val testChannel = Channel<String>()

        val logical = Logical(
            httpClient = okHttpClient,
            jsonParser = moshi,
            config = config { address(baseUrl) }
        )
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) = runBlocking {
                testChannel.send(e.message ?: "failure")
            }

            override fun onResponse(call: Call, response: Response) = runBlocking {
                testChannel.send(response.body()?.string() ?: "success")
            }

        }
        logical.asyncRead("secret", callback)

        testChannel.receive() shouldEqual SECRET_RESPONSE
    }

    @Test
    fun `use callback to write to vault`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"
        val testChannel = Channel<Int>()

        val logical = Logical(
            httpClient = okHttpClient,
            jsonParser = moshi,
            config = config { address(baseUrl) }
        )
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) = runBlocking {
                testChannel.send(-1)
                throw e
            }

            override fun onResponse(call: Call, response: Response) = runBlocking {
                testChannel.send(response.code())
            }

        }
        val expectedRequest = mapOf("key" to "value")
        logical.asyncWrite(
            path = "secret",
            values = expectedRequest,
            responseCallback = callback
        )

        testChannel.receive() shouldEqual 204
        val jsonRequest = server.takeRequest()?.run {
            moshi.adapter<Map<String, Any>>(Map::class.java).fromJson(body)
        } ?: emptyMap()
        jsonRequest shouldEqual expectedRequest
    }
}