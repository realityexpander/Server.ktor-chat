package com.realityexpander.plugins

import com.realityexpander.room.RoomController
import com.realityexpander.routes.chatSocket
import com.realityexpander.routes.getAllMessages
import io.ktor.routing.*
import io.ktor.application.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    // Inject with Koin
    val roomController by inject<RoomController>()

    install(Routing) {
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}
