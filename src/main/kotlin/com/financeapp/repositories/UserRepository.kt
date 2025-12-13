package com.financeapp.repositories

import com.financeapp.database.DatabaseFactory.dbQuery
import com.financeapp.database.tables.UsersTable
import com.financeapp.models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.format.DateTimeFormatter
import java.util.UUID

class UserRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun findByEmail(email: String): Pair<User, String>? = dbQuery {
        UsersTable.select { UsersTable.email eq email }
            .map { row -> rowToUserWithHash(row) }
            .singleOrNull()
    }

    suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.select { UsersTable.id eq id }
            .map { row -> rowToUser(row) }
            .singleOrNull()
    }

    suspend fun create(email: String, passwordHash: String, fullName: String): User = dbQuery {
        val insertStatement = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.fullName] = fullName
        }
        
        val row = insertStatement.resultedValues?.first()
            ?: throw IllegalStateException("Failed to create user")
        
        rowToUser(row)
    }

    suspend fun emailExists(email: String): Boolean = dbQuery {
        UsersTable.select { UsersTable.email eq email }
            .count() > 0
    }

    private fun rowToUser(row: ResultRow): User = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        fullName = row[UsersTable.fullName],
        createdAt = row[UsersTable.createdAt].format(dateFormatter)
    )

    private fun rowToUserWithHash(row: ResultRow): Pair<User, String> = Pair(
        rowToUser(row),
        row[UsersTable.passwordHash]
    )
}
