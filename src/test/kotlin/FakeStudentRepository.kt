package com.studentify

import com.studentify.model.Student
import com.studentify.model.StudentRepository

class FakeStudentRepository: StudentRepository {
    override suspend fun allStudents() = students

    override suspend fun studentCount() = students.size

    override suspend fun studentByName(name: String): Student? = students.find {
        it.name.equals(name, ignoreCase = true)
    }

    override suspend fun studentsByGroup(group: String) = students.filter {
        it.group == group
    }

    override suspend fun addStudent(student: Student) {
        students.add(student)
    }

    override suspend fun removeStudent(student: Student) {
        students.remove(student)
    }

    private val students = mutableListOf(
        Student(
            id = 1,
            name = "Иванов Алексей Сергеевич",
            age = 19,
            group = "CS-101",
            email = "ivanov@edu.ru",
            password = "securePass123",
            tokens = 150
        ),
        Student(
            id = 2,
            name = "Петрова Мария Дмитриевна",
            age = 20,
            group = "CS-101",
            email = "petrova@edu.ru",
            password = "qwerty4321",
            tokens = 85
        ),
        Student(
            id = 3,
            name = "Сидоров Артём Викторович",
            age = 21,
            group = "AI-202",
            email = "sidorov@edu.ru",
            password = "artem2000",
            tokens = 200
        ),
        Student(
            id = 4,
            name = "Кузнецова Елена Александровна",
            age = 20,
            group = "AI-202",
            email = "kuznetsova@edu.ru",
            password = "lenochka99",
            tokens = 50
        ),
        Student(
            id = 5,
            name = "Васильев Денис Олегович",
            age = 21,
            group = "CS-101",
            email = "vasilev@edu.ru",
            password = "den123456",
            tokens = 120
        )
    )
}