package com.studentify.model

import com.studentify.db.TeachersDAO
import com.studentify.db.suspendTransaction
import com.studentify.db.teacherDaoToModel

class PostgreTeacherRepository: TeacherRepository {
    override suspend fun teacherById(id: Int): Teacher? = suspendTransaction {
        TeachersDAO.findById(id)?.let(::teacherDaoToModel)
    }
}