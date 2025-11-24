package de.rogallab.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.Globals
import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.dtos.PersonDto

@Database(
   entities = [
      PersonDto::class
   ],
   version = Globals.databaseVersion,
   exportSchema = false
)
@TypeConverters(LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
   abstract fun createPersonDao(): IPersonDao
}