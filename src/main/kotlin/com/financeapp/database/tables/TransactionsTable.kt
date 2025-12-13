package com.financeapp.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object TransactionsTable : Table("transactions") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val categoryId = uuid("category_id").references(CategoriesTable.id, onDelete = ReferenceOption.RESTRICT)
    val amount = double("amount")
    val currencyCode = varchar("currency_code", 10)
    val note = text("note").nullable()
    val date = date("date")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
