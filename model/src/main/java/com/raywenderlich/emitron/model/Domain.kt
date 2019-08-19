package com.raywenderlich.emitron.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = Domain.TABLE_NAME
)
class Domain {

  @PrimaryKey
  @ColumnInfo(name = "_id")
  var id: String = ""

  @ColumnInfo(name = "domain_id")
  var domainId: String = ""

  @ColumnInfo(name = "name")
  var name: String = ""

  @ColumnInfo(name = "level")
  var level: String = ""

  @ColumnInfo(name = "description")
  var description: String = ""

  companion object {
    const val TABLE_NAME = "domains"
  }
}
