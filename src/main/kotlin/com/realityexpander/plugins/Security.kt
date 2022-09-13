package com.realityexpander.plugins

import com.realityexpander.session.ChatSession
import io.ktor.sessions.*
import io.ktor.application.*
import io.ktor.util.*

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    // Get session for current user
    intercept(ApplicationCallPipeline.Features) {

        if(call.sessions.get<ChatSession>() == null) {
            val username = call.parameters["username"] ?: "Guest"
            call.sessions.set(ChatSession(username, generateNonce())) // generate a session ID (nonce)
        }
    }
}
