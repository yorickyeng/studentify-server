package com.studentify.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: Int,
    val name: String,
    val age: Int,
    val group: String,
    val email: String,
    val password: String,
    val tokens: Int,
)