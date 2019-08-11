package com.raywenderlich.emitron.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.emitron.model.Domain

@Database(entities = [Domain::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

  companion object {

    // For Singleton instantiation
    @Volatile
    private var instance: AppDatabase? = null

    private const val DATABASE_NAME: String = "emitron_db"

    fun getInstance(context: Context): AppDatabase {
      return instance ?: synchronized(this) {
        instance ?: buildDatabase(context).also { instance = it }
      }
    }

    private fun buildDatabase(context: Context): AppDatabase {
      return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
        .build()
    }
  }
}
