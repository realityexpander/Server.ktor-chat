package com.realityexpander.room

import com.realityexpander.data.MessageDataSource
import com.realityexpander.data.model.Message
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class RoomController(
    private val messageDataSource: MessageDataSource
) {
    // Track the members for this room
    private val members = ConcurrentHashMap<String, Member>() // <username, Member>, ConcurrentHashMap is thread-safe

    fun onJoin(
        username: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        // Check for existing member name
        if(members.containsKey(username)) {
            throw MemberAlreadyExistsException()
        }

        // Add member to room
        members[username] = Member(
            username = username,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun sendMessage(senderUsername: String, message: String) {
        // Create the new message
        val messageEntity = Message(
            text = message,
            username = senderUsername,
            timestamp = System.currentTimeMillis()
        )

        // Save the message to the database
        messageDataSource.insertMessage(messageEntity)

        // Send the message to all members via websockets
        members.values.forEach { member ->
            val parsedMessage = Json.encodeToString(messageEntity)

            member.socket.send(Frame.Text(parsedMessage))
        }
    }

    suspend fun getAllMessages(): List<Message> {
        return messageDataSource.getAllMessages()
    }


    // Remove member from room
    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()

        if(members.containsKey(username)) {
            members.remove(username)
        }
    }
}