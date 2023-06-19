/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.delish.data.recipes.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import app.delish.data.db.recipes.entities.RecipeEntity
import com.elbehiry.model.Recipe
import com.elbehiry.model.RecipesItem
import kotlinx.coroutines.flow.Flow

interface RecipesRepository {
    fun searchRecipes(
        query: String?,
        cuisine: String?
    ): Flow<PagingData<RecipesItem>>

    suspend fun getRecipeInformation(
        id: Int?
    ): RecipesItem

    suspend fun getRandomRecipes(
        tags: String?,
        number: Int?
    ): List<Recipe>

    fun getRecipes(): Flow<PagingData<RecipeEntity>>
    suspend fun deleteRecipe(recipeId: Int?)
    suspend fun isRecipeSaved(parameters: Int?): Boolean
    suspend fun saveRecipe(recipe: RecipesItem)
}
