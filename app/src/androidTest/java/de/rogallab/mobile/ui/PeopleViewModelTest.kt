package de.rogallab.mobile.ui


import com.google.common.truth.Truth.assertThat
import de.rogallab.mobile.data.repositories.PeopleRepositoryImpl
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.utilsTest.MainDispatcherRule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PeopleViewModelTest {


   private val _testDispatcher = UnconfinedTestDispatcher()
   @get:Rule
   val mainDispatcherRule = MainDispatcherRule(_testDispatcher)

   var exceptionHandler = CoroutineExceptionHandler { _, exception ->
      val message = exception.localizedMessage?.let {
         logError("ok>Test", it)
         //_uiStateFlow.value = UiState.Error(message)
      } ?: run {
         exception.stackTrace.forEach {
            logError("ok>Test", it.toString())
         }
      }
   }

   @Test
   fun findById() = runTest {
      // Arrange
      var repository: IPeopleRepository = PeopleRepositoryImpl(
         _dispatcher = _testDispatcher,
         _exceptionHandler = exceptionHandler
      )
      var viewModel = PeopleViewModel(
         _repository = repository,
         _dispatcher = _testDispatcher
      )
      val person = Person("Max", "Mustermann",
         "m.mustermann@gmail.com", "05826 1234-5678", "/images/images.jpg")
      viewModel.onFirstNameChange(person.firstName)
      viewModel.onLastNameChange(person.lastName)
      viewModel.onEmailChange(person.email!!)
      viewModel.onPhoneChange(person.phone!!)
      viewModel.onImagePathChange(person.imagePath!!)
      viewModel.add()

      // Assert
      viewModel.readById(person.id)
      var uiStateFlow: UiState<Person> = UiState.Empty
      viewModel.uiStateFlow.collect{ it ->
            uiStateFlow = it
      }
      val isTrue: Boolean = uiStateFlow is UiState.Success<Person>
      logDebug("ok>Test", "uiStateFlow is UiState.Success<Person>:$isTrue")
      val actual = (uiStateFlow as UiState.Success<Person>).data!!
      assertThat(actual).isEqualTo(person)
   }
}





