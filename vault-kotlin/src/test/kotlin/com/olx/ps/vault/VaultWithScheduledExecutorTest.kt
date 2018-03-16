package com.olx.ps.vault

import com.bettercloud.vault.VaultException
import com.olx.ps.vault.utils.MockVault
import com.olx.ps.vault.utils.RenewableCredential
import com.olx.ps.vault.utils.RenewableListener
import com.olx.ps.vault.utils.emptyResponse
import com.olx.ps.vault.utils.initialDbResponse
import com.olx.ps.vault.utils.secretResponse
import com.olx.ps.vault.utils.server
import com.olx.ps.vault.utils.updatedDbResponse
import io.kotlintest.matchers.haveKey
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.time.Duration
import java.util.concurrent.TimeUnit.SECONDS

private const val VAULT_PORT = 8200

class VaultWithScheduledExecutorTest {

    private val credentials = RenewableCredential(
        username = "user",
        password = "123",
        leaseDuration = Duration.ofSeconds(5)
    )

    private val vaultConfig = config {
        address("http://localhost:$VAULT_PORT")
        token("E592D3F7-640C-4CF7-AA4F-40547E97A3CD")
    }

    // TODO - Junit5 repeat test
    @Test
    fun `credentials successfully updated from the scheduler`() = runBlocking {
        val responses = listOf(
            initialDbResponse to 200,
            updatedDbResponse to 200
        )
        val vault = VaultWithScheduledExecutor(vaultConfig)
        server(VAULT_PORT, MockVault(responses)) {
            val listener = RenewableListener(vault, Duration.ofSeconds(2))
            val path = "secret/renewable"
            val initialCredentials = vault
                .logical(Duration.ofMillis(100))
                .read(path, ::RenewableCredential, listener)
            initialCredentials shouldEqual credentials

            // 1 sec is not enough time for the credentials to be renewed
            delay(1, SECONDS)
            val oldCredentials = listener.getLatestCredentials()
            initialCredentials shouldEqual oldCredentials

            // 2 + 1 sec is now enough time to expect the credentials to be renewed
            delay(2, SECONDS)
            val newCredentials = listener.getLatestCredentials()
            newCredentials shouldEqual credentials.copy(password = "321")

            vault.stop()
        }
    }

    @Test
    fun `old credentials are available even if an exception occurs`() = runBlocking {
        val responses = listOf(initialDbResponse to 200)
        val vault = vaultWithScheduledExecutor {
            vaultConfig
        }
        server(VAULT_PORT, MockVault(responses)) {
            val listener = RenewableListener(vault, Duration.ofSeconds(2))
            val path = "secret/renewable"
            val initialCredentials = vault
                .logical(Duration.ofMillis(100))
                .read(path, ::RenewableCredential, listener)

            credentials shouldEqual initialCredentials

            delay(1, SECONDS)
            val oldCredentials = listener.getLatestCredentials()
            oldCredentials shouldEqual initialCredentials

            delay(2, SECONDS)
            val newCredentials = listener.getLatestCredentials()
            newCredentials shouldEqual initialCredentials

            vault.stop()
        }
    }

    @Test
    fun `valid read only at third attempt`() {
        val responses = listOf(
            emptyResponse to 400,
            emptyResponse to 400,
            secretResponse to 200
        )
        server(VAULT_PORT, MockVault(responses)) {
            val vaultWithRetries = vaultWithScheduledExecutor {
                vaultConfig
            }.withRetries(3, 10)

            val logicalWithRetries = vaultWithRetries.logical()
            val secret = logicalWithRetries.read("secret/hello")

            secret.data should haveKey("value1")
        }
    }

    @Test
    fun `exception if not possible to read a secret after 3 attempts`() {
        val responses = mutableListOf(
            emptyResponse to 400,
            emptyResponse to 400,
            emptyResponse to 400
        )
        server(VAULT_PORT, MockVault(responses)) {
            val vaultWithRetries = vaultWithScheduledExecutor {
                vaultConfig
            }.withRetries(3, 10)

            val logicalWithRetries = vaultWithRetries.logical()

            shouldThrow<VaultException> {
                logicalWithRetries.read("secret/hello")
            }
        }
    }
}
