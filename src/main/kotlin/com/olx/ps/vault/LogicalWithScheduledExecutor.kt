package com.olx.ps.vault

import com.bettercloud.vault.VaultConfig
import com.bettercloud.vault.api.Logical
import com.bettercloud.vault.response.LogicalResponse
import java.time.Duration

/**
 * The implementing class for Vault's core/logical operations (e.g. read, write).
 *
 * This class is not intended to be constructed directly. Rather, it is meant to be used by way of
 * [VaultWithScheduledExecutor].
 */
class LogicalWithScheduledExecutor(
    private val schedule: (Duration, () -> Unit) -> Unit,
    private val delay: Duration,
    vaultConfig: VaultConfig
) : Logical(vaultConfig) {

    init {
        if (delay.isZero || delay.isNegative)
            throw IllegalStateException("Duration ${delay.toMillis()}ms is not valid")
    }

    /**
     * Used to read a secret from Vault and then construct an instance of [VaultRenewable] from the
     * [com.bettercloud.vault.response.LogicalResponse], while setting a [VaultListener] for the retrieved secret.
     * [path] is the location in Vault where to retrieve the secret.
     *
     * @throws [com.bettercloud.vault.VaultException] If any errors occurs with the REST request
     * (e.g. non-200 status code, invalid JSON payload, etc), and the maximum number of retries is exceeded.
     */
    public fun <T : VaultRenewable> read(
        path: String,
        constructor: (LogicalResponse) -> T,
        listener: VaultListener<T>
    ): T {
        val renewable = this.read(path, constructor)
        if (renewable.isRenewable) {
            listener.setRenewable(renewable)
            schedule(delay) { listener.checkStatus(renewable, path, constructor) }
        }
        return renewable
    }

    /**
     * Used to read a secret from Vault and then construct an instance of [T] from the
     * [com.bettercloud.vault.response.LogicalResponse]. [path] is the location in Vault where to retrieve the secret.
     *
     * @throws [com.bettercloud.vault.VaultException] If any errors occurs with the REST request
     * (e.g. non-200 status code, invalid JSON payload, etc), and the maximum number of retries is exceeded.
     */
    public fun <T> read(path: String, constructor: (LogicalResponse) -> T): T =
        this.read(path).run(constructor)
}
