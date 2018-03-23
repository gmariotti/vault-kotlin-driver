package com.olx.ps.vault.coroutines

import com.olx.ps.vault.api.Logical
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

public suspend fun Logical.read(path: String): Response = suspendCancellableCoroutine { cont ->
    asyncRead(path, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (cont.isCancelled) {
                call.cancel()
                return
            }
            cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }
    })
}