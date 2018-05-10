package com.olx.ps.vault.coroutines

import arrow.core.Try
import com.olx.ps.vault.api.Logical
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

public suspend fun Logical.read(path: String): Try<Response> = Try {
    suspendCancellableCoroutine<Response> { cont ->
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
}

public suspend fun Logical.write(path: String, values: Map<String, Any>): Try<Response> = Try {
    suspendCancellableCoroutine<Response> { cont ->
        asyncWrite(path = path, values = values, responseCallback = object : Callback {
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
}