package com.olx.ps.vault.extensions

import java.time.Duration

/**
 * Extension function to get a [Duration] into its seconds representation.
 *
 * **!** Conversion is done from [Long] to [Int], which may involve rounding or truncation.
 */
fun Duration.toSeconds(): Int = this.seconds.toInt()
