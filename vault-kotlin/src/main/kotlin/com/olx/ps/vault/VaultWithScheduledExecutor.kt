package com.olx.ps.vault

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * This class extends [com.bettercloud.vault.Vault] with the possibility to set an instance of
 * [ScheduledExecutorService]. The idea is that the [ScheduledExecutorService] should be used for listening on secrets
 * recovered from Vault, for example by periodically renew them if they are close to expiration or similar.
 *
 * The default value for the [executor] is an instance of [Executors.newSingleThreadExecutor].
 */
class VaultWithScheduledExecutor(
    private val vaultConfig: VaultConfig,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
) : Vault(vaultConfig) {
    private val scheduledFutures = ConcurrentLinkedDeque<ScheduledFuture<*>>()
    private val logger = LoggerFactory.getLogger(VaultWithScheduledExecutor::class.java)

    /**
     * Creates an instance of [LogicalWithScheduledExecutor] for scheduling tasks in the [ScheduledExecutorService] and
     * for Vault's core/logical operations (e.g. read, write). [period] is used by the [LogicalWithScheduledExecutor] to
     * schedule the period between different executions of the task by the [ScheduledExecutorService].
     */
    public fun logical(period: Duration): LogicalWithScheduledExecutor =
        LogicalWithScheduledExecutor(this::schedule, period, vaultConfig)

    override fun withRetries(maxRetries: Int, retryIntervalMilliseconds: Int): VaultWithScheduledExecutor {
        super.withRetries(maxRetries, retryIntervalMilliseconds)
        return this
    }

    /**
     * Cancel all scheduled tasks for this instance.
     */
    public fun stop(): Unit { // ktlint-disable no-unit-return
        scheduledFutures.onEach { it.cancel(true) }
    }

    private fun schedule(delay: Duration, task: () -> Unit) {
        Try {
            val scheduledFuture = executor.scheduleAtFixedRate(
                task,
                0,
                delay.toMillis(),
                MILLISECONDS
            )
            scheduledFutures.add(scheduledFuture)
        }.onFailure { logger.error(it.message) }
    }
}
