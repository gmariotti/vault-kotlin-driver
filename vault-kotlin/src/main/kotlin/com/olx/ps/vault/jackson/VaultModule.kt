package com.olx.ps.vault.jackson

import com.bettercloud.vault.VaultConfig
import com.fasterxml.jackson.databind.module.SimpleModule

/**
 * Module for [VaultConfig] deserialization. The deserializer currently support only the following [VaultConfig] fields:
 * - address
 * - token
 * - openTimeout as Duration
 * - readTimeout as Duration
 */
class VaultModule : SimpleModule() {

    init {
        addDeserializer(VaultConfig::class.java, VaultConfigDeserializer)
    }

    override fun getModuleName() = this::class.simpleName

    companion object {
        const val serialVersionUid = 1L
    }
}
