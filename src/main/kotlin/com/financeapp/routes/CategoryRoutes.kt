package com.financeapp.routes

import com.financeapp.models.CreateCategoryRequest
import com.financeapp.plugins.getUserId
import com.financeapp.services.CategoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.categoryRoutes() {
    val categoryService by inject<CategoryService>()

    authenticate("auth-jwt") {
        route("/categories") {
            get {
                val userId = call.getUserId()
                val categories = categoryService.getCategories(userId)
                call.respond(HttpStatusCode.OK, categories)
            }

            post {
                val userId = call.getUserId()
                val request = call.receive<CreateCategoryRequest>()

                val category = categoryService.createCategory(
                    userId = userId,
                    name = request.name,
                    type = request.type,
                    iconCode = request.iconCode
                )

                call.respond(HttpStatusCode.Created, category)
            }
        }
    }
}
