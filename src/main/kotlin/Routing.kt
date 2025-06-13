package com.studentify

import com.studentify.model.Student
import com.studentify.model.StudentRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: StudentRepository) {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal error: ${cause.message}")
        }
    }

    routing {
        // Получить всех студентов
        get("/students") {
            val students = repository.allStudents()
            call.respond(students)
        }

        // Получить количество студентов
        get("/students/count") {
            val count = repository.studentCount()
            call.respond(mapOf("count" to count))
        }

        // Найти студента по имени
        get("/students/name/{name}") {
            val name = call.parameters["name"] ?: throw IllegalArgumentException("Name parameter missing")
            val student = repository.studentByName(name)
            if (student != null) {
                call.respond(student)
            } else {
                call.respond(HttpStatusCode.NotFound, "Student not found")
            }
        }

        // Найти студентов по группе
        get("/students/group/{group}") {
            val group = call.parameters["group"] ?: throw IllegalArgumentException("Group parameter missing")
            val students = repository.studentsByGroup(group)
            call.respond(students)
        }

        // Добавить нового студента
        post("/students") {
            val student = call.receive<Student>()
            repository.addStudent(student)
            call.respond(HttpStatusCode.Created, mapOf("id" to student.id))
        }

        // Обновить данные студента
        put("/students/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val updatedStudent = call.receive<Student>()

            // Проверяем существование студента
            val existing = repository.studentByName(updatedStudent.name)
            if (existing == null || existing.id != id) {
                call.respond(HttpStatusCode.NotFound, "Student not found")
                return@put
            }

            repository.removeStudent(existing)
            repository.addStudent(updatedStudent.copy(id = id))
            call.respond(HttpStatusCode.OK)
        }

        // Удалить студента
        delete("/students/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val student = repository.allStudents().find { it.id == id }
            if (student != null) {
                repository.removeStudent(student)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound, "Student not found")
            }
        }

        // Тестовые маршруты
        get("/") {
            call.respondText("Studentify API is running!")
        }

        get("/test") {
            call.respondText("<h1>Test Page</h1>", ContentType.Text.Html)
        }
    }
}
