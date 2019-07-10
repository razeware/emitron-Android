package com.raywenderlich.emitron.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.exoplayer2.database.ExoDatabaseProvider.DATABASE_NAME
import com.raywenderlich.emitron.model.Collection

@Database(entities = [Collection::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

  companion object {

    // For Singleton instantiation
    @Volatile
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return instance ?: synchronized(this) {
        instance ?: buildDatabase(context).also { instance = it }
      }
    }

    private fun buildDatabase(context: Context): AppDatabase {
      return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
        .addCallback(object : RoomDatabase.Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
          }
        })
        .build()
    }
  }
}
