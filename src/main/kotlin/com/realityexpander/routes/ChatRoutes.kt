package com.realityexpander.routes

import com.realityexpander.room.MemberAlreadyExistsException
import com.realityexpander.room.RoomController
import com.realityexpander.session.ChatSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.chatSocket(roomController: RoomController) {
    webSocket("/chat-socket") {
        val session = call.sessions.get<ChatSession>()

        if(session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }

        try {
            // Add the user to the room
            roomController.onJoin(
                username = session.username,
                sessionId = session.sessionId,
                socket = this
            )

            // Listen for incoming messages
            incoming.consumeEach { frame ->
                if(frame is Frame.Text) {
                    // Broadcast the message to all users in the room
                    roomController.sendMessage(
                        senderUsername = session.username,
                        message = frame.readText()
                    )
                }
            }
        } catch(e: MemberAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            roomController.tryDisconnect(session.username)
        }
    }
}

fun Route.getAllMessages(roomController: RoomController) {
    get("/messages") {
        call.respond(
            HttpStatusCode.OK,
            roomController.getAllMessages()
        )
    }
}