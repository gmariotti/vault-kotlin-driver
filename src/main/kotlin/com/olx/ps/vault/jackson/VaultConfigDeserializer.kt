package com.olx.ps.vault.jackson

import com.bettercloud.vault.VaultConfig
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.olx.ps.vault.config
import com.olx.ps.vault.extensions.toSeconds
import com.olx.ps.vault.internal.VAULT_ADDRESS
import com.olx.ps.vault.internal.VAULT_OPEN_TIMEOUT
import com.olx.ps.vault.internal.VAULT_READ_TIMEOUT
import com.olx.ps.vault.internal.VAULT_TOKEN
import java.time.Duration

/**
 * Jackson deserializer for [com.bettercloud.vault.VaultConfig].
 */
object VaultConfigDeserializer : StdDeserializer<VaultConfig>(VaultConfig::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): VaultConfig {
        val node = parser.codec.readTree<JsonNode>(parser)

        val address = node.get(VAULT_ADDRESS)?.asText()
        val token = node.get(VAULT_TOKEN)?.asText()
        val openTimeout = node.get(VAULT_OPEN_TIMEOUT)?.asText()?.run { Duration.parse(this) }
        val readTimeout = node.get(VAULT_READ_TIMEOUT)?.asText()?.run { Duration.parse(this) }

        return config {
            address(address)
            token(token)
            openTimeout(openTimeout?.toSeconds())
            readTimeout(readTimeout?.toSeconds())
        }
    }
}

/**
 * Used to return a Jackson [com.fasterxml.jackson.databind.module.SimpleModule] containing the following:
 * - [com.bettercloud.vault.VaultConfig] deserializer.
 */
fun createVaultModule(): SimpleModule = SimpleModule().apply {
    addDeserializer(VaultConfig::class.java, VaultConfigDeserializer)
}
