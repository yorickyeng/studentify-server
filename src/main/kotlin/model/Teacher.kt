package com.studentify.model

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: Int,
    val name: String,
    val age: Int,
    val email: String,
    val password: String,
)