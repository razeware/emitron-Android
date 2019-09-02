package com.raywenderlich.emitron.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store domains to database
 */
@Entity(
    tableName = Domain.TABLE_NAME
)
class Domain {

  /**
   * Domain id [Data.id]
   */
  @PrimaryKey
  @ColumnInfo(name = "id")
  var domainId: String = ""

  /**
   * Domain name [Data.getName]
   */
  @ColumnInfo(name = "name")
  var name: String? = ""

  /**
   * Domain name [Data.getLevel]
   */
  @ColumnInfo(name = "level")
  var level: String? = ""

  companion object {

    /**
     * Table name to store domains
     */
    const val TABLE_NAME: String = "domains"

    /**
     * Create list of [Domain] from list of [Data]
     *
     * @return list of [Domain]
     */
    fun listFrom(domains: List<Data>): List<Domain> = domains.map {
      Domain().apply {
        domainId = it.id!!
        name = it.getName()
        level = it.getLevel()
      }
    }
  }
}
