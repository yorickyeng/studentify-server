package com.studentify.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.studentify.db.*
import com.studentify.model.Role
import com.studentify.auth.JwtConfig

class AuthService {
    suspend fun register(request: RegisterRequest): AuthResponse {
        return suspendTransaction {
            // Проверка уникальности email
            val emailExists = when (request.role) {
                Role.STUDENT -> StudentsDAO.find { StudentsTable.email eq request.email }.count() > 0
                Role.TEACHER -> TeachersDAO.find { TeachersTable.email eq request.email }.count() > 0
            }

            if (emailExists) {
                throw IllegalArgumentException("Email already registered")
            }

            // Хеширование пароля
            val passwordHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

            when (request.role) {
                Role.STUDENT -> {
                    // Проверка обязательных полей для студента
                    val group = request.group ?: throw IllegalArgumentException("Group is required for students")

                    val student = StudentsDAO.new {
                        name = request.name
                        age = request.age
                        this.group = group
                        email = request.email
                        password = passwordHash
                        tokens = 0
                    }

                    AuthResponse(
                        token = JwtConfig.generateToken(student.id.value, Role.STUDENT),
                        userId = student.id.value,
                        role = Role.STUDENT
                    )
                }

                Role.TEACHER -> {
                    val teacher = TeachersDAO.new {
                        name = request.name
                        age = request.age
                        email = request.email
                        password = passwordHash
                    }

                    AuthResponse(
                        token = JwtConfig.generateToken(teacher.id.value, Role.TEACHER),
                        userId = teacher.id.value,
                        role = Role.TEACHER
                    )
                }
            }
        }
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return suspendTransaction {
            // Поиск студента
            val student = StudentsDAO.find { StudentsTable.email eq request.email }.singleOrNull()
            student?.let {
                if (BCrypt.verifyer().verify(request.password.toCharArray(), it.password).verified) {
                    return@suspendTransaction AuthResponse(
                        token = JwtConfig.generateToken(it.id.value, Role.STUDENT),
                        userId = it.id.value,
                        role = Role.STUDENT
                    )
                }
            }

            // Поиск преподавателя
            val teacher = TeachersDAO.find { TeachersTable.email eq request.email }.singleOrNull()
            teacher?.let {
                if (BCrypt.verifyer().verify(request.password.toCharArray(), it.password).verified) {
                    return@suspendTransaction AuthResponse(
                        token = JwtConfig.generateToken(it.id.value, Role.TEACHER),
                        userId = it.id.value,
                        role = Role.TEACHER
                    )
                }
            }

            throw IllegalArgumentException("Invalid credentials")
        }
    }
}