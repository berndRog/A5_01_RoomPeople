// PeopleViewModelTest.kt
package de.rogallab.mobile.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import de.rogallab.mobile.SeedTest
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.repositories.PeopleRepository
import de.rogallab.mobile.data.seed.Seed
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.di.domainModules
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.errors.ErrorResources
import de.rogallab.mobile.ui.errors.ResourceProvider
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.utilsTest.MainDispatcherRule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PeopleViewModelTest : KoinTest {

   private val _isInMemory = true
   private val _dbName = "test-database"
   private val _database: AppDatabase by inject()
   private val _repository: IPeopleRepository by inject()
   private val _errorResources: ErrorResources by inject()
   private lateinit var _viewModel: PeopleViewModel

   private val _seed = SeedTest

   @get:Rule
   val mainDispatcherRule = MainDispatcherRule()

   private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
      exception.localizedMessage?.let { it ->
         logError("[PeopleViewModelTest]", it)
      }
   }

   @Before
   fun setup() {
      stopKoin() // Stop any existing Koin instance

      startKoin {
         androidContext(ApplicationProvider.getApplicationContext())
         modules(
            domainModules,
            module {
               single { ResourceProvider(androidContext()) }
               single { ErrorResources(get()) }
               single {
                  if(_isInMemory) {
                     Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase::class.java
                     ).allowMainThreadQueries().build()
                  } else {
                     Room.databaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase::class.java,
                        _dbName
                     ).allowMainThreadQueries().build()
                  }
               }
               single { get<AppDatabase>().createPeopleDao() }
               single<IPeopleRepository> {
                  PeopleRepository(
                     _peopleDao = get(),
                     _dispatcher = get(),
                     _exceptionHandler = exceptionHandler
                  )
               }
            }
         )
      }
      _viewModel = PeopleViewModel(
         _repository = _repository,
         _dispatcher = mainDispatcherRule.testDispatcher,
         _errorResources = _errorResources
      )
   }

   @After
   fun teardown() {
      _database.close()
      if(!_isInMemory) {
         val context = ApplicationProvider.getApplicationContext<Context>()
         val dbFile = context.getDatabasePath(_dbName)
         dbFile.delete()
      }
   }

   @Test
   fun fetchPerson() = runTest {
      // Arrange
      val id = _viewModel.getPersonId()
      val person = _seed.person1Dto.copy(
         id = id,
         imagePath = "/images/images.jpg"
      ).toPerson()

      _viewModel.onFirstNameChange(person.firstName)
      _viewModel.onLastNameChange(person.lastName)
      _viewModel.onEmailChange(person.email!!)
      _viewModel.onPhoneChange(person.phone!!)
      _viewModel.onImagePathChange(person.imagePath!!)
      _viewModel.createPerson()
      advanceUntilIdle()

      // Act
      _viewModel.fetchPerson(person.id)
      advanceUntilIdle()

      // Assert
      val foundPerson = _viewModel.personUiStateFlow.value.person
      assertThat(foundPerson).isEqualTo(person)
   }

   @Test
   fun createPerson() = runTest {
      // Arrange
      val person = _seed.person1Dto.copy(
         id = _viewModel.getPersonId(),
         imagePath = "/images/images.jpg"
      ).toPerson()

      _viewModel.onFirstNameChange(person.firstName)
      _viewModel.onLastNameChange(person.lastName)
      _viewModel.onEmailChange(person.email!!)
      _viewModel.onPhoneChange(person.phone!!)
      _viewModel.onImagePathChange(person.imagePath!!)

      // Act
      _viewModel.createPerson()
      advanceUntilIdle()

      // Assert
      val result = _repository.findById(person.id)
      advanceUntilIdle()

      assertThat(result).isEqualTo(ResultData.Success(person))
   }

   @Test
   fun updatePerson() = runTest {
      // Arrange
      val person = _seed.person1Dto.copy(
         id = _viewModel.getPersonId(),
         imagePath = "/images/images.jpg"
      ).toPerson()
      _viewModel.onFirstNameChange(person.firstName)
      _viewModel.onLastNameChange(person.lastName)
      _viewModel.onEmailChange(person.email!!)
      _viewModel.onPhoneChange(person.phone!!)
      _viewModel.onImagePathChange(person.imagePath!!)
      _viewModel.createPerson()
      advanceUntilIdle()
      // Act
      val updatedPerson = person.copy(lastName = "Adler", email = "a.adler@freenet.de")
      _viewModel.onLastNameChange(updatedPerson.lastName)
      _viewModel.onEmailChange(updatedPerson.email!!)
      _viewModel.updatePerson()
      advanceUntilIdle()
      // Assert
      val result = _repository.findById(updatedPerson.id)
      assertThat(result).isEqualTo(ResultData.Success(updatedPerson))
   }


//
//   @Test
//   fun getAllPeople() = runTest {
//      // Arrange
//      val people = listOf(
//         Person("Max", "Mustermann", "m.mustermann@gmail.com", "05826 1234-5678", "/images/images.jpg"),
//         Person("John", "Doe", "j.doe@gmail.com", "01234 5678-910", "/images/john.jpg")
//      )
//      people.forEach {
//         viewModel.onFirstNameChange(it.firstName)
//         viewModel.onLastNameChange(it.lastName)
//         viewModel.onEmailChange(it.email!!)
//         viewModel.onPhoneChange(it.phone!!)
//         viewModel.onImagePathChange(it.imagePath!!)
//         viewModel.add()
//      }
//
//      // Act
//      val result = viewModel.getAll().first()
//
//      // Assert
//      assertThat(result).isEqualTo(ResultData.Success(people))
//   }
//
//   @Test
//   fun deletePerson() = runTest {
//      // Arrange
//      val person = Person("Max", "Mustermann", "m.mustermann@gmail.com", "05826 1234-5678", "/images/images.jpg")
//      viewModel.onFirstNameChange(person.firstName)
//      viewModel.onLastNameChange(person.lastName)
//      viewModel.onEmailChange(person.email!!)
//      viewModel.onPhoneChange(person.phone!!)
//      viewModel.onImagePathChange(person.imagePath!!)
//      viewModel.add()
//
//      // Act
//      viewModel.remove(person)
//
//      // Assert
//      val result = repository.findById(person.id)
//      assertThat(result).isEqualTo(ResultData.Success(null))
//   }
}