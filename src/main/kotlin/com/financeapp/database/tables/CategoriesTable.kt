package com.financeapp.database.tables

import com.financeapp.models.CategoryType
import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("categories") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id).nullable()
    val name = varchar("name", 100)
    val type = enumerationByName<CategoryType>("type", 20)
    val iconCode = varchar("icon_code", 50)
    val isDeleted = bool("is_deleted").default(false)

    override val primaryKey = PrimaryKey(id)
}
