package com.studentify.db

import com.studentify.model.Student
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object StudentsTable : IntIdTable("students") {
    val name = varchar("name", 50)
    val age = integer("age")
    val group = varchar("group", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 255)
    val tokens = integer("tokens")
}

class StudentsDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StudentsDAO>(StudentsTable)

    var name by StudentsTable.name
    var age by StudentsTable.age
    var group by StudentsTable.group
    var email by StudentsTable.email
    var password by StudentsTable.password
    var tokens by StudentsTable.tokens
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: StudentsDAO) = Student(
    dao.id.value,
    dao.name,
    dao.age,
    dao.group,
    dao.email,
    dao.password,
    dao.tokens,
)