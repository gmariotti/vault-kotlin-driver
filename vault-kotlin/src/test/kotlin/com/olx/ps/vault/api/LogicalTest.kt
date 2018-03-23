package com.olx.ps.vault.api

import com.olx.ps.vault.config
import com.olx.ps.vault.utils.secretResponse
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

class LogicalTest {

    @Test
    fun asyncRead() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(secretResponse))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"
        val testChannel = Channel<String>()

        val okHttpClient = OkHttpClient.Builder().build()
        val logical = Logical(okHttpClient, config { address(baseUrl) })
        val callback = object : Callback {
            override fun onFailure(call: Call?, e: IOException?) = runBlocking {
                testChannel.send(e?.message ?: "failure")
            }

            override fun onResponse(call: Call?, response: Response?) = runBlocking {
                testChannel.send(response?.body()?.string() ?: "success")
            }

        }

        logical.asyncRead("secret", callback)

        testChannel.receive() shouldEqual secretResponse
    }
}