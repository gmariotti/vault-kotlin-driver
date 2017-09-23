package com.olx.ps.vault

import com.olx.ps.vault.utils.GenericCredentials
import com.olx.ps.vault.utils.MockVault
import com.olx.ps.vault.utils.secretResponse
import com.olx.ps.vault.utils.server
import io.kotlintest.matchers.shouldEqual
import org.junit.Test
import java.time.Duration

class LogicalExtensionsTest {

    @Test
    fun `construct data class from LogicalResponse`() {
        val handler = MockVault(mutableListOf(secretResponse to 200))
        server(8200, handler) {
            val vault = vault {
                config {
                    address("http://localhost:8200")
                    token("E592D3F7-640C-4CF7-AA4F-40547E97A3CD")
                    openTimeout(5)
                    readTimeout(10)
                }
            }
            val expected = GenericCredentials("secret1", Duration.ofHours(768))
            val actual = vault.logical().read("secret/hello", ::GenericCredentials)
            expected shouldEqual actual
        }
    }
}
