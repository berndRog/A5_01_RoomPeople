package de.rogallab.mobile.ui.people

import NavScreen
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.people.composables.HandleUiStateError
import de.rogallab.mobile.ui.people.composables.LogUiStates
import de.rogallab.mobile.ui.people.composables.SetSwipeBackground
import de.rogallab.mobile.ui.people.composables.SetCardElevation

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PeopleSwipeListScreen(
   navController: NavController,
   viewModel: PeopleViewModel
) {
   val tag = "ok>PeopleListScreen   ."

   val uiStateFlow: UiState<Person> by viewModel.uiStateFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStateFlow,"UiStateFlow", tag )

   val uiStateListFlow: UiState<List<Person>> by viewModel.uiStateListFlow.collectAsStateWithLifecycle()
   LogUiStates(uiStateListFlow,"UiStateListFlow", tag )

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.people_list)) },
            navigationIcon = {
               val activity = LocalContext.current as Activity
               IconButton(
                  onClick = {
                     logDebug(tag, "Lateral Navigation: finish app")
                     activity.finish()
                  }) {
                  Icon(imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
               // FAB clicked -> InputScreen initialized
               logDebug(tag, "Forward Navigation: FAB clicked")
               viewModel.clearState()
               // Navigate to PersonDetail and put PeopleList on the back stack
               navController.navigate(route = NavScreen.PersonInput.route)
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
         }
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { innerPadding ->

      if (uiStateListFlow == UiState.Empty) {
         logDebug(tag, "uiStatePeople.Empty")
         // nothing to do
      } else if (uiStateListFlow == UiState.Loading) {
         logDebug(tag, "uiStatePeople.Loading")
         Column(
            modifier = Modifier
               .padding(bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp)
               .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
         ) {
            CircularProgressIndicator(modifier = Modifier.size(160.dp))
         }
      } else if (uiStateListFlow is UiState.Success<List<Person>> ||
         uiStateListFlow is UiState.Error ||
         uiStateFlow is UiState.Error) {

         var list: MutableList<Person> = remember { mutableListOf<Person>() }
         if (uiStateListFlow is UiState.Success) {
            list = (uiStateListFlow as UiState.Success<List<Person>>).data as MutableList<Person>
            logDebug(tag, "uiStatePeople.Success items.size ${list.size}")
         }

         LazyColumn(
            modifier = Modifier
               .padding(top = innerPadding.calculateTopPadding())
               .padding(bottom = innerPadding.calculateBottomPadding())
               .padding(horizontal = 8.dp),
            state = rememberLazyListState()
         ) {
            items(items = list) { person ->

           //  var personRemoved: Person?
               val dismissState = rememberDismissState(
                  confirmValueChange = {
                     if (it == DismissValue.DismissedToEnd) {
                        logDebug("ok>SwipeToDismiss()", "-> Detail")
                        navController.navigate(NavScreen.PersonDetail.route + "/${person.id}")
                        return@rememberDismissState true
                     } else if (it == DismissValue.DismissedToStart) {
                        logDebug("ok>SwipeToDismiss()", "-> Delete")
               //       personRemoved = person
                        viewModel.remove(person.id)
                        navController.navigate(NavScreen.PeopleList.route)
                        return@rememberDismissState true
                     }
                     return@rememberDismissState false
                  }
               )

               SwipeToDismiss(
                  state = dismissState,
                  modifier = Modifier.padding(vertical = 4.dp),
                  directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                  background = {
                     SetSwipeBackground(dismissState)
                  },
                  dismissContent = {
                     Column {
                        PersonListCard(
                           firstName = person.firstName,
                           lastName = person.lastName,
                           email = person.email,
                           phone = person.phone,
                           imagePath = person.imagePath ?: "",
                           elevation = SetCardElevation(dismissState)
                        )
                     }
                  }
               )
            }
         }
         if (uiStateListFlow is UiState.Error) {
            HandleUiStateError<List<Person>>(
               uiStateFlow = uiStateListFlow,
               actionLabel = "Ok",
               onErrorAction = { },
               snackbarHostState = snackbarHostState,
               navController = navController,
               routePopBack = NavScreen.PeopleList.route,
               onUiStateFlowChange = { },
               tag = tag
            )
         }
         if (uiStateFlow is UiState.Error) {
            HandleUiStateError(
               uiStateFlow = uiStateFlow,
               actionLabel = "Ok",
               onErrorAction = { },
               snackbarHostState = snackbarHostState,
               navController = navController,
               routePopBack = NavScreen.PeopleList.route,
               onUiStateFlowChange = { viewModel.onUiStateFlowChange(it) },
               tag = tag
            )
         }
      }

   }
}

@Composable
fun PersonListCard(
   firstName: String,
   lastName: String,
   email: String?,
   phone: String?,
   imagePath: String?,
   elevation: CardElevation
) {

   Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = elevation
   ) {
      Column(modifier = Modifier
         .padding(vertical = 4.dp)
         .padding(horizontal = 8.dp)
      ) {
         Text(
            text = "$firstName $lastName",
            style = MaterialTheme.typography.bodyLarge,
         )
         email?.let {
            Text(
               modifier = Modifier.padding(top = 4.dp),
               text = it,
               style = MaterialTheme.typography.bodyMedium
            )
         }
         phone?.let {
            Text(
               text = phone,
               style = MaterialTheme.typography.bodyMedium,
               modifier = Modifier
            )
         }
      }
   }
}