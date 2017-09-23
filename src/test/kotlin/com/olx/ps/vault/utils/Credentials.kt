package com.olx.ps.vault.utils

import com.bettercloud.vault.response.LogicalResponse
import com.olx.ps.vault.VaultRenewable
import java.time.Duration

data class RenewableCredential(
    val username: String,
    val password: String,
    override val leaseId: String = "",
    override val isRenewable: Boolean = true,
    override val leaseDuration: Duration = Duration.ofDays(365)
) : VaultRenewable {

    constructor(logicalResponse: LogicalResponse) : this(
        username = logicalResponse.data.getValue("username"),
        password = logicalResponse.data.getValue("password"),
        leaseId = logicalResponse.leaseId,
        isRenewable = logicalResponse.renewable,
        leaseDuration = Duration.ofSeconds(logicalResponse.leaseDuration)
    )
}

data class GenericCredentials(val value1: String, val leaseDuration: Duration) {

    constructor(logicalResponse: LogicalResponse) : this(
        value1 = logicalResponse.data.getValue("value1"),
        leaseDuration = Duration.ofSeconds(logicalResponse.leaseDuration)
    )
}
