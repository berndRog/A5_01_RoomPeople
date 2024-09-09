package de.rogallab.mobile.ui.base

import androidx.lifecycle.ViewModel
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorUiState
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.cancellation.CancellationException

open class BaseViewModel(
//   private val _imagesRepository: ImagesRepository?,
   private val _dispatcher: CoroutineDispatcher,
   private val _tag: String
) : ViewModel() {

   // ExceptionHandler
   protected val _exceptionHandler = CoroutineExceptionHandler { _, exception ->
      if (exception is CancellationException) {
         // onErrorEvent(exception)
         exception.localizedMessage?.let { message ->
            logError(_tag, message)
         } ?: run {
            exception.stackTrace.forEach {
               logError(_tag, it.toString())
            }
         }
      }
   }
   // Coroutine Context
   private val _coroutineContext = SupervisorJob() + _dispatcher + _exceptionHandler
   // Coroutine Scope
   private val _coroutineScope = CoroutineScope(_coroutineContext)
   override fun onCleared() {
      // cancel all coroutines, when lifecycle of the viewmodel ends
      logDebug(_tag, "Cancel all child coroutines")
      _coroutineContext.cancelChildren()
      _coroutineContext.cancel()
   }


   // Error  State = ViewModel (one time) events
   private val _errorUiStateFlow: MutableStateFlow<ErrorUiState> = MutableStateFlow(ErrorUiState())
   val errorUiStateFlow: StateFlow<ErrorUiState> = _errorUiStateFlow.asStateFlow()

   fun onErrorEvent(params: ErrorParams) {
      logDebug(_tag, "onErrorEvent()")
      _errorUiStateFlow.update { it: ErrorUiState ->
         it.copy(params = params)
      }
   }
   fun onErrorEventHandled() {
      logDebug(_tag, "onErrorEventHandled()")
      _errorUiStateFlow.update { it: ErrorUiState ->
         it.copy(params = null)
      }
   }

   fun onFailure(throwable: Throwable, navEvent: NavEvent? = null) {
      when (throwable) {
         is CancellationException -> {
            val error = throwable.localizedMessage ?: "Cancellation error"
            _errorUiStateFlow.value = _errorUiStateFlow.value.copy(
               params = ErrorParams(message = error, navEvent = navEvent)
            )
         }
         /*
         is RedirectResponseException -> {
            val error = "Redirect error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ClientRequestException -> {
            val error = "Client error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ServerResponseException -> {
            val error = "Server error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ConnectTimeoutException ->
            onErrorEvent(ErrorParams(message = "Connect timed out", navEvent = navEvent))
         is SocketTimeoutException ->
            onErrorEvent(ErrorParams(message = "Socket timed out", navEvent = navEvent))
         is UnknownHostException ->
            onErrorEvent(ErrorParams(message = "No internet connection", navEvent = navEvent))
         */
         else ->
            onErrorEvent(ErrorParams(throwable = throwable, navEvent = navEvent))
      }
   }

   // Navigation State = ViewModel (one time) UI event
   private val _navUiStateFlow: MutableStateFlow<NavUiState> = MutableStateFlow(NavUiState())
   val navUiStateFlow: StateFlow<NavUiState> = _navUiStateFlow.asStateFlow()

   fun navigateTo(event: NavEvent) {
      logVerbose(_tag, "navigateTo() event:${event.toString()}")
      if (event == _navUiStateFlow.value.event) return
      _navUiStateFlow.update { it: NavUiState ->
         it.copy(event = event)
      }
   }
   fun onNavEventHandled() {
      logVerbose(_tag, "onNavEventHandled() event: null")
      _navUiStateFlow.update { it: NavUiState ->
         it.copy(event = null)
      }
   }
}