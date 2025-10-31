package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.RecipeIngredientsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientsDao {
    @Query("SELECT * FROM RecipeIngredients")
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredientsEntity>>
    
    @Query("SELECT * FROM RecipeIngredients WHERE id = :id")
    suspend fun getRecipeIngredientById(id: Int): RecipeIngredientsEntity?
    
    @Query("SELECT * FROM RecipeIngredients WHERE recipe_slug = :recipeSlug")
    fun getIngredientsByRecipe(recipeSlug: String): Flow<List<RecipeIngredientsEntity>>
    
    @Query("SELECT * FROM RecipeIngredients WHERE ingredient_slug = :ingredientSlug")
    fun getRecipesByIngredient(ingredientSlug: String): Flow<List<RecipeIngredientsEntity>>
    
    @Query("SELECT * FROM RecipeIngredients WHERE slug = :slug")
    suspend fun getRecipeIngredientBySlug(slug: String): RecipeIngredientsEntity?
    
    @Query("SELECT * FROM RecipeIngredients WHERE sync_status != 2 AND business_slug = :businessSlug")
    suspend fun getUnsyncedIngredients(businessSlug: String): List<RecipeIngredientsEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredientsEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredientsEntity>)
    
    @Update
    suspend fun updateRecipeIngredient(ingredient: RecipeIngredientsEntity)
    
    @Delete
    suspend fun deleteRecipeIngredient(ingredient: RecipeIngredientsEntity)
    
    @Query("DELETE FROM RecipeIngredients WHERE id = :id")
    suspend fun deleteRecipeIngredientById(id: Int)
    
    @Query("DELETE FROM RecipeIngredients WHERE recipe_slug = :recipeSlug")
    suspend fun deleteIngredientsByRecipe(recipeSlug: String)
    
    @Query("DELETE FROM RecipeIngredients WHERE slug = :slug")
    suspend fun deleteRecipeIngredientBySlug(slug: String)
    
    @Query("DELETE FROM RecipeIngredients")
    suspend fun deleteAllRecipeIngredients()
    
    @Query("SELECT MAX(id) FROM RecipeIngredients")
    suspend fun getMaxId(): Int?
}

