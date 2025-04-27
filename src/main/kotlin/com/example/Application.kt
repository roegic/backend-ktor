package com.example

import io.ktor.server.application.*
import com.example.repository.DatabaseFactory

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
