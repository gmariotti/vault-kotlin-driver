package com.olx.ps.vault.utils

import com.bettercloud.vault.Vault
import com.bettercloud.vault.response.LogicalResponse
import com.olx.ps.vault.VaultListener
import com.olx.ps.vault.read
import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class RenewableListener(private val vault: Vault, private val beforeExpiration: Duration)
    : VaultListener<RenewableCredential> {

    private val lock = ReentrantLock(true)
    private lateinit var expirationDate: LocalDateTime
    private lateinit var renewable: RenewableCredential

    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun setRenewable(renewable: RenewableCredential) {
        updateRenewable(renewable)
    }

    override fun checkStatus(
        currentRenewable: RenewableCredential,
        path: String,
        constructor: (LogicalResponse) -> RenewableCredential
    ) {
        val beforeExpirationDate = expirationDate.minus(beforeExpiration)
        val currentDateWithExtraTime = LocalDateTime.now().plus(beforeExpiration)
        if (currentDateWithExtraTime in beforeExpirationDate..expirationDate) {
            Try {
                val newRenewable = vault.logical().read(path, constructor)
                this.onChange(currentRenewable, newRenewable)
            }.onFailure { logger.warn("Error recovering new credentials -> ${it.message}") }
        }
    }

    override fun onChange(oldRenewable: RenewableCredential, newRenewable: RenewableCredential) {
        updateRenewable(newRenewable)
        logger.info("Credentials updated")
    }

    fun getLatestCredentials(): RenewableCredential =
        lock.withLock { renewable.copy() }

    private fun updateRenewable(newRenewable: RenewableCredential) {
        lock.withLock {
            this.renewable = newRenewable
            this.expirationDate = LocalDateTime.now().plus(newRenewable.leaseDuration)
        }
    }
}
