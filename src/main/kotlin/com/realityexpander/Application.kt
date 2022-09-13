package com.realityexpander

import com.realityexpander.di.mainModule
import io.ktor.application.*
import com.realityexpander.plugins.*
import org.koin.ktor.ext.Koin

// Deploy to Hostinger.com Ubuntu Virtual Server

// start.ktor.io to generate a new KTOR project

// Test on piesocket.com/websocket-tester
// Connect: ws://localhost:8082/chat-socket
// Send: {"type":"message","data":"Hello World"}


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    // Configure Koin (Dependency Injection)
    install(Koin) {
        modules(mainModule)
    }

    configureSockets()
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureSecurity()
}
