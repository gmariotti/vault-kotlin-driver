package com.olx.ps.vault.utils

import com.bettercloud.vault.EnvironmentLoader
import com.bettercloud.vault.VaultConfig

fun configWithEnvironmentLoader(environmentLoader: EnvironmentLoader): VaultConfig {
    val protectedMethod = VaultConfig::class.java.getDeclaredMethod(
        "environmentLoader", EnvironmentLoader::class.java
    ).apply {
        isAccessible = true
    }
    return protectedMethod.invoke(VaultConfig(), environmentLoader) as? VaultConfig
        ?: throw IllegalStateException()
}
