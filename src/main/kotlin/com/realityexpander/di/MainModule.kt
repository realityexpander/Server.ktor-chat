package com.realityexpander.di

import com.realityexpander.data.MessageDataSource
import com.realityexpander.data.MessageDataSourceImpl
import com.realityexpander.room.RoomController
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {

    // single == singleton
    single<CoroutineDatabase> {
        KMongo.createClient()
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