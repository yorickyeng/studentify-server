package com.studentify.model

import com.studentify.db.StudentsDAO
import com.studentify.db.StudentsTable
import com.studentify.db.studentDaoToModel
import com.studentify.db.suspendTransaction

class PostgreStudentRepository: StudentRepository {
    override suspend fun allStudents(): List<Student> = suspendTransaction {
        StudentsDAO.all().map(::studentDaoToModel)
    }

    override suspend fun studentCount(): Int = suspendTransaction {
        StudentsDAO.count().toInt()
    }

    override suspend fun studentById(id: Int): Student? = suspendTransaction {
        StudentsDAO
            .find { StudentsTable.id eq id }
            .limit(1)
            .map(::studentDaoToModel)
            .firstOrNull()
    }

    override suspend fun studentByName(name: String): Student? = suspendTransaction {
        StudentsDAO
            .find { StudentsTable.name eq name }
            .limit(1)
            .map(::studentDaoToModel)
            .firstOrNull()
    }

    override suspend fun studentsByGroup(group: String): List<Student> = suspendTransaction {
        StudentsDAO
            .find { StudentsTable.group eq group }
            .map(::studentDaoToModel)
    }

    override suspend fun addStudent(student: Student): Unit = suspendTransaction {
        StudentsDAO.new {
            name = student.name
            age = student.age
            group = student.group
            email = student.email
            password = student.password
            tokens = student.tokens
        }
    }

    override suspend fun removeStudent(student: Student): Unit = suspendTransaction {
        StudentsDAO.findById(student.id)?.delete()
    }
}