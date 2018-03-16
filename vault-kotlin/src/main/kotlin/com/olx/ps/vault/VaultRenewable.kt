package com.olx.ps.vault

import java.time.Duration

/**
 * This interface should be implemented by Vault secrets that needs the following default information
 * stored for each Vault secret:
 * - lease_id
 * - renewable
 * - lease_duration
 */
interface VaultRenewable {
    /**
     * Vault lease_id associated to this secret.
     */
    public val leaseId: String

    /**
     * Vault renewable to indicate if a secret can be renew or not.
     */
    public val isRenewable: Boolean

    /**
     * Vault lease_duration to indicate the time to live of this secret in Vault.
     */
    public val leaseDuration: Duration
}
