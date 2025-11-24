package de.rogallab.mobile.test.data.datastore

import app.cash.turbine.test
import de.rogallab.mobile.Globals
import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.dtos.PersonDto
import de.rogallab.mobile.data.mapping.toPersonDto
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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

// problems with java version 17 and android sdk 36
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class) // <- nutzt deine TestApp
class IPersonDaoUt: KoinTest {
   @get:Rule
   val tempDir = TemporaryFolder()
   @get:Rule
   val mainRule = MainDispatcherRule()

   private lateinit var _seed: Seed
   private lateinit var _database: AppDatabase
   private lateinit var _personDao: IPersonDao
   private lateinit var _seedDatabase: SeedDatabase
   private lateinit var _seedPeopleDto: List<PersonDto>

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
      _seedDatabase = koin.get<SeedDatabase>()

      // expected
      _seedDatabase.seedPerson()
      _seedPeopleDto = _seed.people.map{ it: Person ->
         it.toPersonDto() }
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
   fun selectAll_ok() = runTest {
      // arrange
      val expectedDtos = _seedPeopleDto
      // act: use turbine to access flow
      _personDao.selectAll().test {
         val actualDtos = awaitItem()
         assertEquals(_seedPeopleDto.size, actualDtos.size)
         assertContentEquals(expectedDtos, actualDtos)
      }
   }

   @Test
   fun findById_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val expectedDto = requireNotNull(_seedPeopleDto.firstOrNull { it.id == id })
      // act
      val actualDto = _personDao.findById(id)
      // assert
      assertEquals(expectedDto, actualDto)
   }

   @Test
   fun insert_ok() = runTest{
      // arrange
      val personDto = Person(
         "Bernd", "Rogalla", "b-u.rogalla@ostfalia.de", null,
         id = "00000001-0000-0000-0000-000000000000").toPersonDto()
      // act
      _personDao.insert(personDto)
      // assert
      val actualDto = _personDao.findById(personDto.id)
      assertEquals(personDto, actualDto)
   }

   @Test
   fun update_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val personDto = requireNotNull(_personDao.findById(id))
      // act
      val updatedDto = personDto.copy(lastName ="Albers", email = "a.albers@gmx.de")
      _personDao.update(updatedDto)
      // assert
      val actualDto = _personDao.findById(personDto.id)
      assertEquals(updatedDto, actualDto)
   }

   @Test
   fun delete_ok() = runTest {
      // arrange
      val id = "01000000-0000-0000-0000-000000000000"
      val personDto = requireNotNull(_personDao.findById(id))
      // act
      _personDao.delete(personDto)
      // assert
      val actualDto = _personDao.findById(personDto.id)
      assertNull(actualDto)
   }
}