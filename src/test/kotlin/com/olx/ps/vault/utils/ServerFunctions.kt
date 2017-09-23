package com.olx.ps.vault.utils

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler

inline fun server(port: Int, handler: AbstractHandler, body: () -> Unit) {
    with(Server(port)) {
        this.handler = handler
        try {
            start()
            body()
        } finally {
            stop()
        }
    }
}
