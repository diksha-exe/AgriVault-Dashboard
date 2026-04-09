package com.example.agrivault.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM agricultural_categories")
    fun getAllCategories(): Flow<List<AgriculturalCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: AgriculturalCategory)

    @Query("SELECT * FROM agricultural_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): AgriculturalCategory?

    @Query("SELECT * FROM agricultural_categories")
    suspend fun getAllCategoriesSnapshot(): List<AgriculturalCategory>

    @Query("DELETE FROM agricultural_categories")
    suspend fun deleteAllCategories()
}
