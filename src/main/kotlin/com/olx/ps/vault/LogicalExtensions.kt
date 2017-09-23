@file:JvmName("LogicalExtensions")

package com.olx.ps.vault

import com.bettercloud.vault.api.Logical
import com.bettercloud.vault.response.LogicalResponse

/**
 * Extension function for [com.bettercloud.vault.api.Logical], used to read a secret from Vault stored in [path] and
 * then construct an instance of [T] from the [com.bettercloud.vault.response.LogicalResponse].
 *
 * @throws [com.bettercloud.vault.VaultException] If any errors occurs with the REST request
 * (e.g. non-200 status code, invalid JSON payload, etc), and the maximum number of retries is exceeded.
 */
inline fun <T> Logical.read(path: String, constructor: (LogicalResponse) -> T): T =
    this.read(path).run(constructor)
