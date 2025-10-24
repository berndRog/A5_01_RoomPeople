package de.rogallab.mobile.ui.people.composables

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.Globals
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logComp
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.composables.CollectBy
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.images.ImageViewModel
import de.rogallab.mobile.ui.people.PersonIntent
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
   id: String,
   viewModel: PersonViewModel,
   imageViewModel: ImageViewModel,
   onNavigateReverse: () -> Unit = {},
) {
   val tag = "<-PersonDetailScreen"
   val nComp = remember { mutableIntStateOf(1) }
   SideEffect { logComp(tag, "Composition #${nComp.value++}") }

   val groupName = Globals.FILE_NAME.split(".").first()

   // observe the personUiStateFlow in the ViewModel
   val personUiState = CollectBy(viewModel.personUiStateFlow, tag)

   // fetch person by id
   LaunchedEffect(id) {
      viewModel.handlePersonIntent(PersonIntent.FetchById(id))
   }

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets.safeDrawing, // .safeContent .safeGestures,
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.personDetail)) },
            navigationIcon = {
               IconButton(onClick = {
                  if (viewModel.validate()) {
                     viewModel.handlePersonIntent(PersonIntent.Update)
                     onNavigateReverse()
                  }
               }) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      },
      modifier = Modifier.fillMaxSize()
   ) { innerPadding ->

      Column(
         modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
      ) {
         PersonContent(
            personUiState = personUiState,
            validator = koinInject<PersonValidator>(),
            //imageLoader = koinInject(),
            onFirstNameChange = {
               viewModel.handlePersonIntent(PersonIntent.FirstNameChange(it))
            },
            onLastNameChange = {
               viewModel.handlePersonIntent(PersonIntent.LastNameChange(it))
            },
            onEmailChange = {
               viewModel.handlePersonIntent(PersonIntent.EmailChange(it))
            },
            onPhoneChange = {
               viewModel.handlePersonIntent(PersonIntent.PhoneChange(it))
            },
            onSelectImage = {  imageViewModel.selectImage(it, groupName) { uriString ->
               viewModel.handlePersonIntent(PersonIntent.ImagePathChange(uriString)) }
            },
            onCaptureImage = { imageViewModel.captureImage(it, groupName) { uriString ->
               viewModel.handlePersonIntent(PersonIntent.ImagePathChange(uriString)) }
            },
            handleError = { message -> message?.let {
               viewModel.handlePersonIntent(PersonIntent.ErrorEvent(it)) }
            },
         )
      }
   }

   // Error handling
   ErrorHandler(
      viewModel = viewModel,
      snackbarHostState = snackbarHostState
   )
}