package com.olx.ps.vault

import com.bettercloud.vault.SslConfig
import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * Used to build an instance of [com.bettercloud.vault.Vault] from a [VaultConfig].
 *
 * @throws [com.bettercloud.vault.VaultException] If the address is not set in the call to [config].
 */
inline fun vault(config: () -> VaultConfig): Vault =
    Vault(config().build())

/**
 * Used to build an instance of [VaultWithScheduledExecutor] from a [VaultConfig]. The default value for the [executor]
 * is an instance of [Executors.newSingleThreadExecutor].
 *
 * @throws [com.bettercloud.vault.VaultException] If the address is not set in the call to [config].
 */
inline fun vaultWithScheduledExecutor(
    executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    config: () -> VaultConfig
): VaultWithScheduledExecutor = VaultWithScheduledExecutor(config(), executor)

/**
 * Used to build an instance of [com.bettercloud.vault.VaultConfig].
 *
 * @throws [com.bettercloud.vault.VaultException] If the address is not set in the call to [config].
 */
inline fun config(body: VaultConfig.() -> VaultConfig): VaultConfig =
    VaultConfig().body().build()

/**
 * [com.bettercloud.vault.VaultConfig] extension method for setting [com.bettercloud.vault.SslConfig].
 */
inline fun VaultConfig.sslConfig(body: SslConfig.() -> SslConfig): VaultConfig =
    this.sslConfig(SslConfig().body().build())

/**
 * Used to build an instance of [com.bettercloud.vault.SslConfig].
 *
 * @throws [com.bettercloud.vault.VaultException] If SSL certificate verification is enabled, and any problem occurs
 * while trying to build an SSLContext.
 */
inline fun sslConfig(body: SslConfig.() -> SslConfig): SslConfig =
    SslConfig().body().build()
