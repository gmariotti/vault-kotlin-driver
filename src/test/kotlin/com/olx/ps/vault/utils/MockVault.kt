package com.olx.ps.vault.utils

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MockVault(responsesWithStatus: List<Pair<String, Int>>) : AbstractHandler() {
    private val responses = responsesWithStatus.toMutableList()

    override fun handle(
        target: String,
        baseRequest: Request,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        response.contentType = "application/json"
        baseRequest.isHandled = true
        if (responses.isNotEmpty()) {
            successResponse(response)
        } else {
            failureResponse(response)
        }
    }

    private fun successResponse(response: HttpServletResponse) {
        val (mockResponse, mockStatus) = responses.removeAt(0)
        println("MockVault is sending an HTTP $mockStatus code, with expected payload...")
        response.status = mockStatus
        response.writer.println(mockResponse)
    }

    private fun failureResponse(response: HttpServletResponse) {
        println("MockVault is sending an HTTP 400 code, without a payload")
        response.status = 400
    }
}
