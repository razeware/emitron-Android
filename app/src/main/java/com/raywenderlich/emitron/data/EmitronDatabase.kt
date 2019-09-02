package com.raywenderlich.emitron.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.emitron.model.Category
import com.raywenderlich.emitron.model.Domain

/**
 * Database helper for Emitron
 */
@Database(entities = [Domain::class, Category::class], version = 1, exportSchema = true)
abstract class EmitronDatabase : RoomDatabase() {

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
