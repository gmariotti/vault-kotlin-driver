package com.olx.ps.vault.jackson

import com.bettercloud.vault.VaultConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.matchers.shouldBe
import org.junit.Test

class VaultConfigDeserializerTest {

    private val mapper = jacksonObjectMapper()
        .registerModule(createVaultModule())

    // TODO - Junit5 parametrized
    @Test
    fun `complete configuration is correctly deserialized`() {
        val jsonConfig = """
            {
                "address": "http://localhost",
                "token": "mock_token",
                "openTimeout": "PT10S",
                "readTimeout": "PT5S"
            }
            """.trimIndent()

        val config = mapper.readValue<VaultConfig>(jsonConfig)
        with(config.build()) {
            address shouldBe "http://localhost"
            token shouldBe "mock_token"
            openTimeout shouldBe 10
            readTimeout shouldBe 5
        }
    }

    @Test
    fun `partial configuration is correctly deserialized`() {
        val jsonConfig = """
            {
                "address": "http://localhost",
                "openTimeout": "PT10S",
                "readTimeout": "PT5S"
            }
            """.trimIndent()

        val config = mapper.readValue<VaultConfig>(jsonConfig)
        with(config.build()) {
            address shouldBe "http://localhost"
            openTimeout shouldBe 10
            readTimeout shouldBe 5
        }
    }
}
