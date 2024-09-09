package de.rogallab.mobile.data
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import de.rogallab.mobile.SeedTest
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.repositories.PeopleRepository
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPerson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class PeopleRepositoryTest {

   private lateinit var database: AppDatabase
   private lateinit var peopleDao: IPeopleDao
   private lateinit var repository: IPeopleRepository
   private val dbName = "test-database"

   @Before
   fun setup() {
      database = Room.databaseBuilder(
         ApplicationProvider.getApplicationContext(),
         AppDatabase::class.java,
         dbName
      ).allowMainThreadQueries().build()
      peopleDao = database.createPeopleDao()
      repository = PeopleRepository(
         _peopleDao = peopleDao,
         _dispatcher = Dispatchers.IO,
         _exceptionHandler = CoroutineExceptionHandler { _, _ -> }
      )
   }

   @After
   fun teardown() {
      database.close()
      val context = ApplicationProvider.getApplicationContext<Context>()
      val dbFile = context.getDatabasePath(dbName)
      dbFile.delete()
   }

   @Test
   fun insert_retrievePerson() = runTest {
      // Arrange
      val seed = SeedTest
      val person = seed.person1Dto.toPerson()
      // Act
      repository.create(person)
      // Assert
      val result = repository.findById(person.id)
      assertEquals(ResultData.Success(person), result)
   }

   @Test
   fun update_retrievePerson() = runTest {
      // Arrange
      val seed = SeedTest
      val person = seed.person1Dto.toPerson()
      repository.create(person)
      // Act
      val updatedPerson = person.copy(
         lastName = "Adler",
         email = "a.adler@freenet.de"
      )
      repository.update(updatedPerson)
      // Assert
      val result = repository.findById(person.id)
      assertEquals(ResultData.Success(updatedPerson), result)
   }

   @Test
   fun insertAll_retrieveAllPeople() = runTest {
      // Arrange
      val seed = SeedTest
      val people = listOf(
         seed.person1Dto.toPerson(),
         seed.person2Dto.toPerson(),
         seed.person3Dto.toPerson(),
         seed.person4Dto.toPerson(),
         seed.person5Dto.toPerson(),
         seed.person6Dto.toPerson()
      )
      // Act
      repository.createAll(people)
      // Assert
      val result = repository.getAll().first()
      assertEquals(ResultData.Success(people), result)
   }

   @Test
   fun deleteAllPeople() = runTest {
      // Arrange
      val seed = SeedTest
      val person1 = seed.person1Dto.toPerson()
      val person2 = seed.person2Dto.toPerson()
      repository.create(person1)
      repository.create(person2)
      // Act
      repository.remove(person1)
      repository.remove(person2)
      // Assert
      val result = repository.getAll().first()
      assertEquals(ResultData.Success(emptyList<Person>()), result)
   }

   @Test
   fun deletePerson() = runTest {
      // Arrange
      val seed = SeedTest
      val person = seed.person1Dto.toPerson()
      repository.create(person)
      // Act
      repository.remove(person)
      // Assert
      val result = repository.findById(person.id)
      assertEquals(ResultData.Success(null), result)
   }
}