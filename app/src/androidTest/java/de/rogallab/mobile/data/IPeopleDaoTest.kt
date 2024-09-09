// IPeopleDaoTest.kt
package de.rogallab.mobile.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import de.rogallab.mobile.SeedTest
import de.rogallab.mobile.data.dto.PersonDto
import de.rogallab.mobile.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class IPeopleDaoTest {

   private lateinit var database: AppDatabase
   private lateinit var peopleDao: IPeopleDao

   private val isInMemory = false
   private val dnName = "Testdatabase"

   @Before
   fun setup() {
      // Create the database
      if (isInMemory) {
         database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
         ).allowMainThreadQueries().build()
      } else {
         // Use a real database file
         database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            dnName
         ).allowMainThreadQueries().build()
      }
      // Create the DAO
      peopleDao = database.createPeopleDao()
   }

   @After
   fun teardown() {
      database.close()
      if (!isInMemory) {
         val context = ApplicationProvider.getApplicationContext<Context>()
         val dbFile = context.getDatabasePath(dnName)
         dbFile.delete()
      }
   }

   @Test
   fun insert_retrievePerson() = runBlocking {
      // Arrange
      val seed = SeedTest
      val personDto = seed.person1Dto
      // Act
      peopleDao.insert(personDto)
      // Assert
      val retrievedPerson = peopleDao.selectById(personDto.id)
      assertEquals(personDto, retrievedPerson)
   }

   @Test
   fun update_retrievePerson() = runBlocking {
      // Arrange
      val seed = SeedTest
      val personDto = seed.person1Dto
      peopleDao.insert(personDto)

      // Act
      val updatedDto = personDto.copy(
         lastName = "Adler",
         email = "a.adler@freenet.de"
      )
      peopleDao.update(updatedDto)
      // Assert
      val retrievedPerson = peopleDao.selectById(personDto.id)
      assertEquals(updatedDto, retrievedPerson)
   }

   @Test
   fun insertAll_retrieveAllPeople() = runBlocking {
      // Arrange
      val seed = SeedTest
      val peopleDto = mutableListOf(
         seed.person1Dto,
         seed.person2Dto,
         seed.person3Dto,
         seed.person4Dto,
         seed.person5Dto,
         seed.person6Dto
      )
      // Act
      peopleDao.insertAll(peopleDto)
      // Act
      val foundPeopleDto = withTimeout(500) {
         peopleDao.selectAll().first().toList() // the first emitted value is the peopleDtos list
      }
      // Assert
      assertEquals(foundPeopleDto, peopleDto)
   }

   @Test
   fun deleteAllPeople() = runBlocking {
      // Arrange
      val seed = SeedTest
      val peopleDto = mutableListOf(
         seed.person1Dto,
         seed.person2Dto,
         seed.person3Dto,
         seed.person4Dto,
         seed.person5Dto,
         seed.person6Dto
      )
      peopleDao.insertAll(peopleDto)
      // Act
      peopleDao.deleteAll()
      // Assert
      val foundPeopleDto = withTimeout(500) {
         peopleDao.selectAll().first().toList() // the first emitted value is the peopleDtos list
      }
      assertEquals(emptyList<PersonDto>(), foundPeopleDto)
   }

   @Test
   fun deletePerson() = runBlocking {
      // Arrange
      val seed = SeedTest
      val personDto = seed.person1Dto
      peopleDao.insert(personDto)
      // Act
      peopleDao.delete(personDto)
      // Assert
      val retrievedPerson = peopleDao.selectById(personDto.id)
      assertEquals(null, retrievedPerson)
   }
}