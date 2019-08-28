package com.raywenderlich.emitron.data.filter.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.raywenderlich.emitron.model.Domain

/**
 * Dao for Domains
 */
@Dao
interface DomainDao {

  /**
   * Get observer for domains
   */
  @Query("SELECT * FROM domains")
  fun getDomains(): LiveData<List<Domain>>

  /**
   * Insert domains
   */
  @Insert(onConflict = REPLACE)
  suspend fun insertDomains(domains: List<Domain>)
}
