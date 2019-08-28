package com.raywenderlich.emitron.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store categories to database
 */
@Entity(
  tableName = Category.TABLE_NAME
)
class Category {

  /**
   * Category id [Data.id]
   */
  @PrimaryKey
  @ColumnInfo(name = "id")
  var categoryId: String = ""

  /**
   * Category name [Data.getName]
   */
  @ColumnInfo(name = "name")
  var name: String? = ""

  companion object {

    /**
     * Table name to store categories
     */
    const val TABLE_NAME: String = "categories"

    /**
     * Create list of [Category] from list of [Data]
     *
     * @return list of [Category]
     */
    fun listFrom(domains: List<Data>): List<Category> = domains.map {
      Category().apply {
        categoryId = it.id!!
        name = it.getName()
      }
    }
  }
}
