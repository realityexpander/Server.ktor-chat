package com.realityexpander.data

import com.realityexpander.data.model.Message
import org.litote.kmongo.coroutine.CoroutineDatabase

class MessageDataSourceImpl(
    private val db: CoroutineDatabase  // KMongo database
): MessageDataSource {

    private val messages = db.getCollection<Message>()  // creates the database collection

    override suspend fun getAllMessages(): List<Message> {
        return messages.find()
            .descendingSort(Message::timestamp)
            .toList()
    }

    override suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }
}