package com.studentify.model

interface TeacherRepository {
    suspend fun teacherById(id: Int): Teacher?
}