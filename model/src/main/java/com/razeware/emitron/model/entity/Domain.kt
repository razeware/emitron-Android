package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.model.toRequestFormat

/**
 * Entity to store domains to database
 */
@Entity(
  tableName = Domain.TABLE_NAME
)
data class Domain(

  /**
   * Domain id [Data.id]
   */
  @PrimaryKey
  @ColumnInfo(name = "domain_id")
  val domainId: String,

  /**
   * Domain name [Data.getName]
   */
  val name: String?,

  /**
   * Domain name [Data.getLevel]
   */
  var level: String? = null
) {

  /**
   * Build [Data] from [Domain]
   */
  fun toData(): Data = Data(
    id = domainId,
    type = DataType.Domains.toRequestFormat(),
    attributes = Attributes(
      name = name,
      level = level
    )
  )

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
    fun listFrom(domains: List<Data>): List<Domain> =
      domains.filter { it.isTypeDomain() }.map {
        Domain(
          domainId = it.id!!,
          name = it.getName(),
          level = it.getLevel()
        )
      }
  }
}
