package de.rogallab.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IPeopleDao

@Database(
   entities = [PersonDto::class],
   version = AppStart.database_version,
   exportSchema = false
)

//@TypeConverters(ZonedDateTimeConverters::class)

abstract class AppDatabase : RoomDatabase() {

   // The database exposes DAOs through an abstract "getter" method for each @Dao.
   abstract fun createPeopleDao(): IPeopleDao

}