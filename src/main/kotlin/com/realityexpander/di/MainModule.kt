package com.realityexpander.di

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.realityexpander.data.MessageDataSource
import com.realityexpander.data.MessageDataSourceImpl
import com.realityexpander.room.RoomController
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

// Get Environment Variables
val env: MutableMap<String, String> = System.getenv()
private val mongoSettings: Map<String, String> = mapOf(
    "MONGO_USERNAME"         to (env["MONGO_USERNAME"]         ?: ""),
    "MONGO_PASSWORD"         to (env["MONGO_PASSWORD"]         ?: ""),
    "MONGO_HOST"             to (env["MONGO_HOST"]             ?: "localhost"),
    "MONGO_HOST_POSTFIX"     to (env["MONGO_HOST_POSTFIX"]     ?: ""),
    "MONGO_PORT"             to (env["MONGO_PORT"]             ?: "27017"),
    "MONGO_AUTH_SOURCE"      to (env["MONGO_AUTH_SOURCE"]      ?: "admin"),
    "MONGO_DB"               to (env["MONGO_DB"]               ?: "message_db"),
)

// REFERENCE: server full client connection raw string (for app running on the server):
// from MongoCompass: mongodb://AdminUsername:PasswordXXXX%25%5E%26@localhost:27017/?authMechanism=DEFAULT&authSource=admin
//
// Use this way for raw connection string for local server:
// MONGO_HOST=AdminUsername:PasswordXXXX%25%5E%26@localhost  // Note: uses ascii encoded username and password
// MONGO_HOST_POSTFIX=/?authSource=admin                     // Note: stripped out the "authmechanism" param

// Build the connection string
private val clientConnectionString =
    "mongodb://${mongoSettings["MONGO_HOST"]}" +
            ":${mongoSettings["MONGO_PORT"]}" +
            "${mongoSettings["MONGO_HOST_POSTFIX"]}"
private val credential = MongoCredential.createCredential(
    mongoSettings["MONGO_USERNAME"]!!,
    mongoSettings["MONGO_AUTH_SOURCE"]!!,
    mongoSettings["MONGO_PASSWORD"]!!.toCharArray()
)
private val client =
    MongoClientSettings
        .builder()
        .applyConnectionString(ConnectionString(clientConnectionString))
        .credential(credential)
        .build()

val mainModule = module {

    // single == singleton
    single<CoroutineDatabase> {
        KMongo.createClient(client)
            .coroutine
            .getDatabase("message_db")
    }

    single<MessageDataSource> {
        MessageDataSourceImpl(get())
    }

    single<RoomController> {
        RoomController(get())
    }
}

fun printMongoEnv() {
    runBlocking {
        println("--- MongoDB Environment START ---")
        println("MONGO_USERNAME: ${mongoSettings["MONGO_USERNAME"]}")
        println("MONGO_PASSWORD: ${mongoSettings["MONGO_PASSWORD"]}")
        println("MONGO_HOST: ${mongoSettings["MONGO_HOST"]}")
        println("MONGO_HOST_POSTFIX: ${mongoSettings["MONGO_HOST_POSTFIX"]}")
        println("MONGO_AUTH_SOURCE: ${mongoSettings["MONGO_AUTH_SOURCE"]}")
        println("MONGO_PORT: ${mongoSettings["MONGO_PORT"]}")
        println("MONGO_DB: ${mongoSettings["MONGO_DB"]}")
        println()
        println("clientConnectionString: $clientConnectionString")
        println("credential: $credential")
        println()
        println("client username: ${client.credential?.userName}")
        println("client password: ${client.credential?.password.contentToString()}")
        println("--- MongoDB Environment END ---")
    }
}