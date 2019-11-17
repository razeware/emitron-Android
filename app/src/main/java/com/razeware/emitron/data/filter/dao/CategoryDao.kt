package com.razeware.emitron.data.filter.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.razeware.emitron.model.entity.Category

/**
 * Dao for categories
 */
@Dao
interface CategoryDao {

  /**
   * Get observer for categories
   */
  @Query("SELECT * FROM categories")
  fun getCategories(): LiveData<List<Category>>

  /**
   * Insert categories
   */
  @Insert(onConflict = REPLACE)
  suspend fun insertCategories(categories: List<Category>)

  @Query("DELETE from categories")
  fun deleteAll()
}
