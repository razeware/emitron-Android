package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.model.toRequestFormat

/**
 * Entity to store categories to database
 */
@Entity(
  tableName = Category.TABLE_NAME
)
data class Category(

  /**
   * Category id [Data.id]
   */
  @PrimaryKey
  @ColumnInfo(name = "category_id")
  val categoryId: String,

  /**
   * Category name [Data.getName]
   */
  val name: String?
) {

  /**
   * Build [Data] from [Category]
   */
  fun toData(): Data = Data(
    id = categoryId,
    type = DataType.Categories.toRequestFormat(),
    attributes = Attributes(
      name = name
    )
  )

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
      Category(
        categoryId = it.id!!,
        name = it.getName()
      )
    }
  }
}
