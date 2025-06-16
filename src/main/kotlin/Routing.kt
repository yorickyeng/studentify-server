package com.studentify

import com.studentify.model.*
import com.studentify.service.AuthService
import com.studentify.service.LoginRequest
import com.studentify.service.RegisterRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    authService: AuthService,
    studentRepository: StudentRepository,
    teacherRepository: TeacherRepository
) {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state: ${cause.message}")
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal error: ${cause.message}")
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "Resource not found")
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid request")
        }
    }

    routing {
        get("/") {
            call.respondText("Studentify API is running!")
        }

        // Аутентификация
        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                try {
                    val response = authService.register(request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                try {
                    val response = authService.login(request)
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                }
            }
        }

        authenticate("auth-jwt") {
            route("/students") {
                get {
                    val students = studentRepository.allStudents()
                    call.respond(students)
                }

                get("/count") {
                    val count = studentRepository.studentCount()
                    call.respond(mapOf("count" to count))
                }

                get("/name/{name}") {
                    val name = call.parameters["name"] ?: throw BadRequestException("Name parameter missing")
                    val student = studentRepository.studentByName(name)
                        ?: throw NotFoundException("Student not found")
                    call.respond(student)
                }

                get("/group/{group}") {
                    val group = call.parameters["group"] ?: throw BadRequestException("Group parameter missing")
                    val students = studentRepository.studentsByGroup(group)
                    call.respond(students)
                }

                get("/me") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val role = principal.getClaim("role", String::class)?.let { Role.valueOf(it) }

                    if (role != Role.STUDENT) {
                        throw ForbiddenException("Only students can access this resource")
                    }

                    val userId = principal.getClaim("userId", Int::class)!!
                    val student = studentRepository.studentById(userId)
                        ?: throw NotFoundException("Student not found")

                    call.respond(student)
                }
            }

            // Маршруты только для преподавателей
            get("/teacher/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.getClaim("role", String::class)?.let { Role.valueOf(it) }

                if (role != Role.TEACHER) {
                    throw ForbiddenException("Only teachers can access this resource")
                }

                val userId = principal.getClaim("userId", Int::class)!!
                val teacher = teacherRepository.teacherById(userId)
                    ?: throw NotFoundException("Teacher not found")

                call.respond(teacher)
            }

            post("/students") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.getClaim("role", String::class)?.let { Role.valueOf(it) }

                if (role != Role.TEACHER) {
                    throw ForbiddenException("Only teachers can create students")
                }

                val student = call.receive<Student>()
                studentRepository.addStudent(student)
                call.respond(HttpStatusCode.Created, mapOf("id" to student.id))
            }

            put("/students/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.getClaim("role", String::class)?.let { Role.valueOf(it) }

                if (role != Role.TEACHER) {
                    throw ForbiddenException("Only teachers can update students")
                }

                val id = call.parameters["id"]?.toInt() ?: throw BadRequestException("Invalid ID")
                val updatedStudent = call.receive<Student>()

                // Проверяем существование студента
                val existing = studentRepository.studentById(id)
                    ?: throw NotFoundException("Student not found")

                studentRepository.removeStudent(existing)
                studentRepository.addStudent(updatedStudent.copy(id = id))
                call.respond(HttpStatusCode.OK)
            }

            delete("/students/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.getClaim("role", String::class)?.let { Role.valueOf(it) }

                if (role != Role.TEACHER) {
                    throw ForbiddenException("Only teachers can delete students")
                }

                val id = call.parameters["id"]?.toInt() ?: throw BadRequestException("Invalid ID")
                val student = studentRepository.studentById(id)
                    ?: throw NotFoundException("Student not found")

                studentRepository.removeStudent(student)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

class ForbiddenException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
class BadRequestException(message: String) : Exception(message)