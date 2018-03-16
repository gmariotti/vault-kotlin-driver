package com.olx.ps.vault

import com.bettercloud.vault.VaultException
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.olx.ps.vault.utils.configWithEnvironmentLoader
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import org.junit.Assert.assertTrue
import org.junit.Test

private const val VAULT_ADDR = "VAULT_ADDR"
private const val VAULT_TOKEN = "VAULT_TOKEN"
private const val VAULT_OPEN_TIMEOUT = "VAULT_OPEN_TIMEOUT"
private const val VAULT_READ_TIMEOUT = "VAULT_READ_TIMEOUT"

class VaultKotlinFunctionsTest {

    @Test
    fun `check complete config is correctly created`() {
        val config = config {
            address("localhost")
            token("E592D3F7-640C-4CF7-AA4F-40547E97A3CD")
            openTimeout(5)
            readTimeout(10)
            sslConfig {
                verify(false)
            }
        }
        with(config) {
            address shouldBe "localhost"
            token shouldBe "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
            openTimeout shouldBe 5
            readTimeout shouldBe 10
        }
    }

    @Test
    fun `check partial config is correctly created`() {
        val config = config {
            address("localhost")
            token("E592D3F7-640C-4CF7-AA4F-40547E97A3CD")
            openTimeout(5)
            sslConfig {
                verify(true)
            }
        }
        with(config) {
            token shouldBe "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
            openTimeout shouldBe 5
        }
    }

    @Test
    fun `build sslConfig using custom function`() {
        val sslConfig = sslConfig {
            verify(true)
        }

        with(sslConfig) {
            assertTrue(isVerify)
        }
    }

    @Test
    fun `use some vault environment variables`() {
        val config = configWithEnvironmentLoader(mock {
            on { loadVariable(VAULT_ADDR) } doReturn "http://127.0.0.1:8200"
            on { loadVariable(VAULT_TOKEN) } doReturn "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
        }).build()

        with(config) {
            address shouldBe "http://127.0.0.1:8200"
            token shouldBe "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
        }
    }

    @Test
    fun `use all vault environment variables`() {
        val config = configWithEnvironmentLoader(mock {
            on { loadVariable(VAULT_ADDR) } doReturn "http://127.0.0.1:8200"
            on { loadVariable(VAULT_TOKEN) } doReturn "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
            on { loadVariable(VAULT_OPEN_TIMEOUT) } doReturn "10"
            on { loadVariable(VAULT_READ_TIMEOUT) } doReturn "5"
        }).build()

        with(config) {
            address shouldBe "http://127.0.0.1:8200"
            token shouldBe "E592D3F7-640C-4CF7-AA4F-40547E97A3CD"
            openTimeout shouldBe 10
            readTimeout shouldBe 5
        }
    }

    @Test
    fun `missing address throws a VaultException`() {
        shouldThrow<VaultException> {
            config {
                token("E592D3F7-640C-4CF7-AA4F-40547E97A3CD")
                openTimeout(5)
                readTimeout(10)
            }
        }
    }
}
