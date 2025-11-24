package de.rogallab.mobile.test.data.repositories

import app.cash.turbine.test
import de.rogallab.mobile.Globals
import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.dtos.PersonDto
import de.rogallab.mobile.data.mapping.toPersonDto
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.test.MainDispatcherRule
import de.rogallab.mobile.test.TestApplication
import de.rogallab.mobile.test.di.defModulesTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.file.Path
import kotlin.test.*

// problems with java version 17 and android sdk 36
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class) // keine MainApplication!
class IPersonRepositoryUt : KoinTest {
   @get:Rule
   val tempDir = TemporaryFolder()
   @get:Rule
   val mainRule = MainDispatcherRule()

   private lateinit var _seed: Seed
   private lateinit var _database: AppDatabase
   private lateinit var _personDao: IPersonDao
   private lateinit var _repository: IPersonRepository
   private lateinit var _seedDatabase: SeedDatabase
   private lateinit var _seedPeople: List<Person>

   @Before
   fun setup() = runTest {
      // no logging during testing
      Globals.isInfo = false
      Globals.isDebug = false
      Globals.isVerbose = false
      Globals.isComp = false

      stopKoin() // falls von anderen Tests übrig
      val testModule = defModulesTest(
         appHomePath = tempDir.root.absolutePath,
         ioDispatcher = mainRule.dispatcher() // StandardTestDispatcher als IO
      )
      val koinApp = startKoin { modules(testModule) }
      val koin = koinApp.koin
      _seed = koin.get<Seed>()
      _database = koin.get<AppDatabase>()
      _database.clearAllTables()
      _personDao = koin.get<IPersonDao>()
      _repository = koin.get<IPersonRepository>()
      _seedDatabase = koin.get<SeedDatabase>()

      // expected
      _seedDatabase.seedPerson()
      _seedPeople = _seed.people
   }

   @After
   fun tearDown() {
      _database.clearAllTables()
      _database.close()
      // Delete the database file
      // ApplicationProvider.getApplicationContext<Context>()
      //   .deleteDatabase(AppStart.DATABASENAME + "_Test")
      stopKoin()
   }

   @Test
   fun getAllUt_ok() = runTest {
      // arrange
      val expected = _seedPeople
      // act / assert
      _repository.getAll().test {
         awaitItem()
            .onSuccess { assertContentEquals(expected, it) }
            .onFailure { fail(it.message) }
         cancelAndIgnoreRemainingEvents()
      }
   }

   @Test
   fun insert_emitsUpdateFlow() = runTest {
      // arrange
      val newPerson = Person(
         "Bernd", "Rogalla", "b-u.rogalla@ostfalia.de", null,
         id = "00090001-0000-0000-0000-000000000001"
      )
      // act / assert: subscribe to flow, perform insert, expect another emission containing the new person
      _repository.getAll().test {
         // consume initial emission
         awaitItem()
            .onSuccess { result -> assertTrue(result.size == 26) }
            .onFailure { fail(it.message) }

         // perform insert and assert success
         _repository.create(newPerson)
            .onFailure { fail(it.message) }
            .onSuccess { assertEquals(Unit, it) }

         // await next emission and assert it contains the inserted person
         awaitItem()
            .onSuccess { result ->
               assertTrue(result.any { p -> p.id == newPerson.id && p == newPerson }, "Inserted person not present in emitted list") }
            .onFailure { fail(it.message) }

         cancelAndIgnoreRemainingEvents()
      }
   }

   @Test
   fun findByIdUt_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val expected = _seedPeople.firstOrNull { person -> person.id == id }
      assertNotNull(expected)
      // act / assert
      _repository.findById(id)
         .onSuccess { assertEquals(expected, it) }
         .onFailure { fail(it.message) }
   }

   @Test
   fun insertUt_ok() = runTest {
      // arrange
      val person = Person(
         "Bernd", "Rogalla", "b-u.rogalla@ostfalia.de", null,
         id = "00090001-0000-0000-0000-000000000000")
      // act
      _repository.create(person)
         .onSuccess { assertEquals(Unit, it) }
         .onFailure { fail(it.message) }
      // asser
      _repository.findById(person.id)
         .onSuccess { assertEquals(person, it) }
         .onFailure { fail(it.message) }
   }

   @Test
   fun updateUt_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val person = requireNotNull(_repository.findById(id).getOrNull())
      // act
      val updated = person.copy(lastName = "Albers", email = "a.albers@gmx.de")
      _repository.update(updated)
         .onSuccess { assertEquals(Unit, it) }
         .onFailure { fail(it.message) }
      // assert
      _repository.findById(person.id)
         .onSuccess { assertEquals(updated, it) }
         .onFailure { fail(it.message) }
   }

   @Test
   fun deleteUt_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val person = requireNotNull(_repository.findById(id).getOrNull())
      // act
      _repository.remove(person)
         .onSuccess { assertEquals(Unit, it) }
         .onFailure { fail(it.message) }
      // assert
      _repository.findById(person.id)
         .onSuccess { actual -> assertNull(actual) }
         .onFailure { fail(it.message) }
   }

}