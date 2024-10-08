package de.rogallab.mobile.ui.people

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorResources
import de.rogallab.mobile.ui.navigation.NavEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PeopleViewModel(
   private val _repository: IPeopleRepository,
   private val _errorResources: ErrorResources,
   private val _dispatcher: CoroutineDispatcher
) : BaseViewModel(_dispatcher, TAG) {
   // to undo: store the person that was removed
   private var removedPerson: Person? = null

   init {
      logDebug(TAG, "init")
      //_repository.readDataStore()
   }

   // Data Binding PeopleListScreen <=> PersonViewModel
   private val _peopleUiStateFlow: MutableStateFlow<PeopleUiState> = MutableStateFlow(PeopleUiState())
   val peopleUiStateFlow: StateFlow<PeopleUiState> = _peopleUiStateFlow.asStateFlow()
   // read all people from repository
   fun fetchPeople() {
      viewModelScope.launch(_dispatcher) {
         _repository.getAll().collect { resultData: ResultData<List<Person>> ->
            when (resultData) {
               is ResultData.Success -> {
                  _peopleUiStateFlow.update { it: PeopleUiState ->
                     it.copy(people = resultData.data.toList())
                  }
               }
               is ResultData.Failure -> {
                  val message = "Failed to read people ${resultData.throwable.localizedMessage}"
                  logError(TAG, message)
               }
               else -> Unit
            }
         }
      }
   }
   // Data Binding PersonScreen <=> PersonViewModel
   private val _personUiStateFlow: MutableStateFlow<PersonUiState> = MutableStateFlow(PersonUiState())
   val personUiStateFlow: StateFlow<PersonUiState> = _personUiStateFlow.asStateFlow()

   fun getPersonId(): String = _personUiStateFlow.value.person.id

   fun onFirstNameChange(firstName: String) {
      if (firstName == _personUiStateFlow.value.person.firstName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(firstName = firstName))
      }
   }
   fun onLastNameChange(lastName: String) {
      if (lastName == _personUiStateFlow.value.person.lastName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(lastName = lastName))
      }
   }
   fun onEmailChange(email: String?) {
      if (email == null || email == _personUiStateFlow.value.person.email) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(email = email))
      }
   }
   fun onPhoneChange(phone: String?) {
      if (phone == null || phone == _personUiStateFlow.value.person.phone) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(phone = phone))
      }
   }
   fun onImagePathChange(imagePath: String?) {
      if (imagePath == null || imagePath == _personUiStateFlow.value.person.imagePath) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(imagePath = imagePath))
      }
   }

   fun fetchPerson(personId: String) {
      logDebug(TAG, "fetchPersonById: $personId")
      viewModelScope.launch {
         when (val resultData = _repository.findById(personId)) {
            is ResultData.Success -> {
               _personUiStateFlow.update { it: PersonUiState ->
                  it.copy(person = resultData.data ?: Person())
               }
            }
            is ResultData.Failure -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
            else -> Unit
         }
      }
   }

   fun createPerson() {
      val person = _personUiStateFlow.value.person
      logDebug(TAG, "createPerson ${person.id.as8()}")
      viewModelScope.launch {
         when (val resultData = _repository.create(person)) {
            is ResultData.Success -> Unit
            is ResultData.Failure -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
            else -> Unit
         }
      }
   }

   fun updatePerson() {
      val person = _personUiStateFlow.value.person
      logDebug(TAG, "updatePerson ${person.id.as8()}")
      viewModelScope.launch {
         when (val resultData = _repository.update(person)) {
            is ResultData.Success -> fetchPeople()
            is ResultData.Failure -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
            else -> Unit
         }
      }
   }

   fun removePerson(person: Person) {
      logDebug(TAG, "removePerson: ${person.id.as8()}")
      viewModelScope.launch {
         when (val resultData = _repository.remove(person)) {
            is ResultData.Success -> {
               removedPerson = person
               fetchPeople()
            }
            is ResultData.Failure -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
            else -> Unit
         }
      }
   }

   fun undoRemovePerson() {
      removedPerson?.let { person ->
         viewModelScope.launch {
            when (val resultData = _repository.create(person)) {
               is ResultData.Success -> {
                  removedPerson = null
                  fetchPeople()
               }
               is ResultData.Failure -> {
                  onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
               }
               else -> Unit
            }
         }
      }
   }

   fun clearState() {
      _personUiStateFlow.update { it.copy(person = Person()) }
   }

   fun validateName(name: String): Pair<Boolean, String> =
      if (name.isEmpty() || name.length < _errorResources.charMin)
         Pair(true, _errorResources.nameTooShort)
      else if (name.length > _errorResources.charMax)
         Pair(true, _errorResources.nameTooLong)
      else
         Pair(false, "")

   fun validateEmail(email: String?): Pair<Boolean, String> {
      email?.let {
         when (android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
            true -> return Pair(false, "") // email ok
            false -> return Pair(true, _errorResources.emailInValid)
         }
      } ?: return Pair(false, "")
   }

   fun validatePhone(phone: String?): Pair<Boolean, String> {
      phone?.let {
         when (android.util.Patterns.PHONE.matcher(it).matches()) {
            true -> return Pair(false, "")   // email ok
            false -> return Pair(true, _errorResources.phoneInValid)
         }
      } ?: return Pair(false, "")
   }

   fun validate(
      isInput: Boolean
   ) {
      // input is ok        -> add and navigate up
      // detail is ok       -> update and navigate up
      // is the is an error -> show error and stay on screen
      val charMin = _errorResources.charMin
      val charMax = _errorResources.charMax
      val person = _personUiStateFlow.value.person
      // firstName or lastName too short
      if (person.firstName.isEmpty() || person.firstName.length < charMin) {
         onErrorEvent(ErrorParams(message = _errorResources.nameTooShort, navEvent = null))
      } else if (person.firstName.length > charMax) {
         onErrorEvent(ErrorParams(message = _errorResources.nameTooLong, navEvent = null))
      } else if (person.lastName.isEmpty() || person.lastName.length < charMin) {
         onErrorEvent(ErrorParams(message = _errorResources.nameTooShort, navEvent = null))
      } else if (person.lastName.length > charMax) {
         onErrorEvent(ErrorParams(message = _errorResources.nameTooLong, navEvent = null))
      } else if (person.email != null &&
         !Patterns.EMAIL_ADDRESS.matcher(person.email).matches()) {
         onErrorEvent(ErrorParams(message = _errorResources.emailInValid, navEvent = null))
      } else if (person.phone != null &&
         !Patterns.PHONE.matcher(person.phone).matches()) {
         onErrorEvent(ErrorParams(message = _errorResources.phoneInValid, navEvent = null))
      } else {
         if (isInput) this.createPerson()
         else this.updatePerson()
      }
   }

   companion object {
      private const val TAG = "[PeopleViewModel]"
   }
}