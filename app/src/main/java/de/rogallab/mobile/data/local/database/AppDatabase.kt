package de.rogallab.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.Globals.DATABASE_VERSION
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.dtos.PersonDto

@Database(
   entities = [
      PersonDto::class
   ],
   version = DATABASE_VERSION,
   exportSchema = false
)
@TypeConverters(LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
   abstract fun createPersonDao(): IPersonDao
}