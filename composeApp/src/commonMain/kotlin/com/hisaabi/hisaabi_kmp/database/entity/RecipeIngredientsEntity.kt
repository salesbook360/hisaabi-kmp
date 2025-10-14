package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "RecipeIngredients",
    indices = [Index(value = ["recipe_slug", "ingredient_slug"], unique = true)]
)
data class RecipeIngredientsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipe_slug: String?,
    val ingredient_slug: String?,
    val quantity: Double?,
    val quantity_unit_slug: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

