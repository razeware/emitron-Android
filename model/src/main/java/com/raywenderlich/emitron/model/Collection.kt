package com.raywenderlich.emitron.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = Collection.TABLE_NAME
)
class Collection {

  @PrimaryKey
  @ColumnInfo(name = "_id")
  var id: String = ""

  companion object {
    const val TABLE_NAME = "collections"
  }
}
