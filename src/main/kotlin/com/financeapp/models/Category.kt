package com.financeapp.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Category(
    @Contextual
    val id: UUID,
    @Contextual
    val userId: UUID?,
    val name: String,
    val type: CategoryType,
    val iconCode: String,
    val isDeleted: Boolean = false
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: CategoryType,
    val iconCode: String
)
