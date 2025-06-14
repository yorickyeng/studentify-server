package com.studentify.service

import com.studentify.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val age: Int,
    val email: String,
    val password: String,
    val role: Role,
    val group: String? = null  // Только для студентов
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int,
    val role: Role
)