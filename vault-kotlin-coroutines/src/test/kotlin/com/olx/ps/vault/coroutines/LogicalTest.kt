package com.olx.ps.vault.coroutines

import com.olx.ps.vault.api.Logical
import com.olx.ps.vault.common.test.SECRET_RESPONSE
import com.olx.ps.vault.config
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class LogicalTest {

    @Test
    fun `suspendable read`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(SECRET_RESPONSE))
        server.start()
        val baseUrl = "http://127.0.0.1:${server.port}"

        val okHttpClient = OkHttpClient.Builder().build()
        val logical = Logical(okHttpClient, config { address(baseUrl) })

        val response = logical.read("secret")

        response.body()?.string() shouldEqual SECRET_RESPONSE
    }

    @Test
    fun `exception if something happens`() {
        runBlocking {
            val server = MockWebServer()
            server.start()
            val baseUrl = "http://127.0.0.1:${server.port}"

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MILLISECONDS)
                .readTimeout(10, TimeUnit.MILLISECONDS)
                .build()
            val logical = Logical(okHttpClient, config { address(baseUrl) })

            shouldThrow<SocketTimeoutException> {
                logical.read("secret")
            }
        }
    }
}