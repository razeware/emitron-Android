package com.raywenderlich.emitron.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.emitron.data.content.dao.ContentDao
import com.raywenderlich.emitron.data.content.dao.ContentDomainJoinDao
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.entity.*

/**
 * Database helper for Emitron
 */
@Database(
  entities = [
    Domain::class,
    Category::class,
    Content::class,
    Progression::class,
    ContentDomainJoin::class
  ],
  version = 1,
  exportSchema = true
)
abstract class EmitronDatabase : RoomDatabase() {

  /**
   * See [DomainDao]
   */
  abstract fun domainDao(): DomainDao

  /**
   * See [CategoryDao]
   */
  abstract fun categoryDao(): CategoryDao

  /**
   * See [ContentDao]
   */
  abstract fun contentDao(): ContentDao

  /**
   * See [ContentDomainJoinDao]
   */
  abstract fun contentDomainJoinDao(): ContentDomainJoinDao

  /**
   * See [ProgressionDao]
   */
  abstract fun progressionDao(): ProgressionDao

  companion object {

    // For Singleton instantiation
    @Volatile
    internal var instance: EmitronDatabase? = null
      private set

    private const val DATABASE_NAME: String = "emitron_db"

    /**
     * Build and return an instance of [EmitronDatabase]
     */
    fun getInstance(context: Context): EmitronDatabase {
      return instance ?: synchronized(this) {
        instance ?: buildDatabase(context).also { instance = it }
      }
    }

    private fun buildDatabase(context: Context): EmitronDatabase {
      return Room.databaseBuilder(context, EmitronDatabase::class.java, DATABASE_NAME)
        .build()
    }
  }
}
