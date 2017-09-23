package com.olx.ps.vault

import com.bettercloud.vault.response.LogicalResponse

/**
 * Listener for [VaultRenewable] secrets, should be used together with [VaultWithScheduledExecutor] to periodically
 * listen for renewed secrets.
 */
interface VaultListener<in T : VaultRenewable> {

    /**
     * Used to set the instance of [VaultRenewable] to periodically check.
     */
    public fun setRenewable(renewable: T): Unit

    /**
     * Used to check the status of the current [VaultRenewable], stored in a Vault [path]. The [constructor] is used to
     * build the instance of [VaultRenewable].
     */
    public fun checkStatus(currentRenewable: T, path: String, constructor: (LogicalResponse) -> T)

    /**
     * Used to notify the change from the old [VaultRenewable] to the new one.
     */
    public fun onChange(oldRenewable: T, newRenewable: T): Unit
}
