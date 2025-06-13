package com.studentify

import com.studentify.model.PostgreStudentRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()

    val repository = PostgreStudentRepository()

    configureSerialization()
    configureSecurity()
    configureSockets()
    configureRouting(repository)
}
