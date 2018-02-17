package com.olx.ps.vault.jackson

import com.bettercloud.vault.VaultConfig
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
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
        var address: String? = null
        var token: String? = null
        var openTimeout: Duration? = null
        var readTimeout: Duration? = null

        var currentNode = parser.currentToken()
        if (currentNode.isStructStart) {
            while (!currentNode.isStructEnd) {
                currentNode = parser.nextToken()
                when (parser.currentName) {
                    VAULT_ADDRESS -> address = parser.readExpectedNextToken(VAULT_ADDRESS)
                    VAULT_TOKEN -> token = parser.readExpectedNextToken(VAULT_TOKEN)
                    VAULT_OPEN_TIMEOUT -> openTimeout = Duration.parse(parser.readExpectedNextToken(VAULT_OPEN_TIMEOUT))
                    VAULT_READ_TIMEOUT -> readTimeout = Duration.parse(parser.readExpectedNextToken(VAULT_READ_TIMEOUT))
                }
            }
        }

        return config {
            address(address)
            token(token)
            openTimeout(openTimeout?.toSeconds())
            readTimeout(readTimeout?.toSeconds())
        }
    }
}

private fun JsonParser.readExpectedNextToken(expected: String): String {
    if (!nextToken().isScalarValue)
        throw IllegalArgumentException("Expected '$expected' value")
    return this.valueAsString
}
