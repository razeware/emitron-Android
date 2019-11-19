package com.razeware.emitron.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.razeware.emitron.data.content.dao.*
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.data.progressions.dao.WatchStatDao
import com.razeware.emitron.model.entity.*

/**
 * Database helper for Emitron
 */
@Database(
  entities = [
    Domain::class,
    Category::class,
    Content::class,
    Progression::class,
    ContentDomainJoin::class,
    Group::class,
    ContentGroupJoin::class,
    GroupEpisodeJoin::class,
    Download::class,
    WatchStat::class
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

  /**
   * See [GroupDao]
   */
  abstract fun groupDao(): GroupDao

  /**
   * See [ContentGroupJoinDao]
   */
  abstract fun contentGroupDao(): ContentGroupJoinDao

  /**
   * See [GroupEpisodeJoinDao]
   */
  abstract fun groupEpisodeDao(): GroupEpisodeJoinDao

  /**
   * See [DownloadDao]
   */
  abstract fun downloadDao(): DownloadDao

  /**
   * See [WatchStatDao]
   */
  abstract fun watchStateDao(): WatchStatDao

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
