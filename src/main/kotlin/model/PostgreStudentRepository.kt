package com.studentify.model

import com.studentify.db.StudentsDAO
import com.studentify.db.StudentsTable
import com.studentify.db.daoToModel
import com.studentify.db.suspendTransaction

class PostgreStudentRepository: StudentRepository {
    override suspend fun allStudents(): List<Student> = suspendTransaction {
        StudentsDAO.all().map(::daoToModel)
    }

    override suspend fun studentCount(): Int = suspendTransaction {
        StudentsDAO.count().toInt()
    }

    override suspend fun studentByName(name: String): Student? = suspendTransaction {
        StudentsDAO
            .find { StudentsTable.name eq name }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun studentsByGroup(group: String): List<Student> = suspendTransaction {
        StudentsDAO
            .find { StudentsTable.group eq group }
            .map(::daoToModel)
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