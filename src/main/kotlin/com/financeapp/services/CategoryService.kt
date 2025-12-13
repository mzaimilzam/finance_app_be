package com.financeapp.services

import com.financeapp.models.Category
import com.financeapp.models.CategoryType
import com.financeapp.repositories.CategoryRepository
import java.util.UUID

class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    suspend fun getCategories(userId: UUID): List<Category> {
        return categoryRepository.findAllForUser(userId)
    }

    suspend fun getCategoryById(id: UUID): Category? {
        return categoryRepository.findById(id)
    }

    suspend fun createCategory(
        userId: UUID,
        name: String,
        type: CategoryType,
        iconCode: String
    ): Category {
        require(name.isNotBlank()) { "Category name cannot be blank" }
        require(iconCode.isNotBlank()) { "Icon code cannot be blank" }

        return categoryRepository.create(userId, name.trim(), type, iconCode.trim())
    }

    suspend fun updateCategory(
        id: UUID,
        name: String?,
        type: CategoryType?,
        iconCode: String?
    ): Boolean {
        return categoryRepository.update(id, name?.trim(), type, iconCode?.trim())
    }

    suspend fun deleteCategory(id: UUID): Boolean {
        return categoryRepository.softDelete(id)
    }
}
