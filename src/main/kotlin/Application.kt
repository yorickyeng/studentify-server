package com.studentify

import com.studentify.auth.configureSecurity
import com.studentify.db.configureDatabases
import com.studentify.model.PostgreStudentRepository
import com.studentify.model.PostgreTeacherRepository
import com.studentify.service.AuthService
import com.studentify.auth.JwtConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    JwtConfig.init(environment)

    configureDatabases()
    configureSecurity()
    configureSerialization()
    configureSockets()

    val authService = AuthService()
    val studentRepository = PostgreStudentRepository()
    val teacherRepository = PostgreTeacherRepository()

    configureRouting(
        authService,
        studentRepository,
        teacherRepository
    )
}