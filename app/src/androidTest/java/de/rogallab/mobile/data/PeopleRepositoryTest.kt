package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.repositories.PeopleRepositoryImpl
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.utilsTest.MainDispatcherRule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PeopleRepositoryTest {

   private val _testDispatcher = StandardTestDispatcher()

   @get:Rule
   val mainDispatcherRule = MainDispatcherRule(_testDispatcher)

   var exceptionHandler = CoroutineExceptionHandler { _, exception ->
      val message = exception.localizedMessage?.let {
         logError(PeopleViewModel.tag, it)
         //_uiStateFlow.value = UiState.Error(message)
      } ?: run {
         exception.stackTrace.forEach {
            logError(PeopleViewModel.tag, it.toString())
         }
      }
   }

   @Test
   fun findByIdSuccess() = runTest {
      TestScope().testScheduler

      // Arrange
      val personDto = PersonDto("Erika", "Mustermann", "e.mustermann@t-onlien.de")
      var repository: IPeopleRepository = PeopleRepositoryImpl(
         _dispatcher = _testDispatcher,
         _exceptionHandler = exceptionHandler
      )
      repository.add(personDto)

      // Act
      val actual = repository.findById(personDto.id)
      logDebug("ok>Test", "${actual?.asString()}")
      assert(actual?.asString() == personDto.asString())
   }

}
