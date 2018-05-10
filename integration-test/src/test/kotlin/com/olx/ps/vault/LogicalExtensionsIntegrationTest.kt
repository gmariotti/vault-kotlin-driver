package com.olx.ps.vault

import com.bettercloud.vault.response.LogicalResponse
import com.olx.ps.testContainer.VaultContainer
import com.olx.ps.testContainer.VaultContainer.CERT_PEMFILE
import com.olx.ps.testContainer.VaultContainer.PASSWORD
import com.olx.ps.testContainer.VaultContainer.USER_ID
import io.kotlintest.matchers.shouldEqual
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.time.Duration

@Ignore("Old api")
class LogicalExtensionsIntegrationTest {

    companion object {
        @JvmField
        @ClassRule
        val container = VaultContainer()

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            with(container) {
                initAndUnsealVault()
                setupBackendUserPass()
                vault.auth().loginByUserPass(USER_ID, PASSWORD)
            }
        }
    }

    @Test
    fun `construct data class from LogicalResponse`() {
        val vault = vaultWithScheduledExecutor {
            config {
                address(container.address)
                token(container.rootToken)
                sslConfig {
                    pemFile(File(CERT_PEMFILE))
                }
            }
        }
        with(vault) {
            logical().write("secret/hello", mapOf("secret" to "hello"))
            val expected = GenericCredentials("hello", Duration.ofHours(24))

            val actual = logical().read("secret/hello", ::GenericCredentials)
            actual shouldEqual expected
        }
    }
}

data class GenericCredentials(val secret: String, val leaseDuration: Duration) {

    constructor(response: LogicalResponse) : this(
        secret = response.data.getValue("secret"),
        leaseDuration = Duration.ofSeconds(response.leaseDuration)
    )
}
