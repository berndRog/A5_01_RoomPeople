package de.rogallab.mobile.data.local.database

import androidx.room.RoomDatabase
import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.mapping.toPersonDto
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import org.koin.core.component.KoinComponent

class SeedDatabase(
   private val _database: RoomDatabase,
   private val _personDao: IPersonDao,
   private val _seed: Seed
) : KoinComponent {

   suspend fun seedPerson(): Boolean {
      try {
         _personDao.count().let { count ->
            if (count > 0) {
               logDebug("<-SeedDatabase", "seed: Database already seeded")
               return false
            }
         }
         _seed.createPeopleList()
         _database.clearAllTables()
         _personDao.insert(_seed.people.map { it.toPersonDto() })
         return true
      } catch (e: Exception) {
         logError("<-SeedDatabase", "seed: ${e.message}")
      }
      return false
   }
}