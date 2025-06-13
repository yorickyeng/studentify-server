package com.studentify.model

interface StudentRepository {
    suspend fun allStudents(): List<Student>
    suspend fun studentCount(): Int
    suspend fun studentByName(name: String): Student?
    suspend fun studentsByGroup(group: String): List<Student>
    suspend fun addStudent(student: Student)
    suspend fun removeStudent(student: Student)
}