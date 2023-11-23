package de.rogallab.mobile.ui.people

import NavScreen
import android.app.Activity
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.UiState
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import showErrorMessage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PeopleSwipeListScreen(
   navController: NavController,
   viewModel: PeopleViewModel
) {
   val tag = "ok>PeopleListScreen   ."

   val uiStateFlow: UiState<Person>
      by viewModel.uiStateFlow.collectAsStateWithLifecycle()

   val uiStateListFlow: UiState<List<Person>>
      by viewModel.uiStateListFlow.collectAsStateWithLifecycle()


   val upState = uiStateFlow.upHandler
   val backState = uiStateFlow.backHandler
   if(uiStateFlow is UiState.Empty)
      Log.v(tag,"Composition UiState.Empty $upState $backState")
   else if(uiStateFlow is UiState.Loading)
      Log.v(tag,"Composition UiState.Loading $upState $backState")
   else if(uiStateFlow is UiState.Success)
      Log.v(tag,"Composition UiState.Success $upState $backState")
   else if(uiStateFlow is UiState.Error)
      Log.v(tag,"Composition UiState.Error $upState $backState")

   val upStateList = uiStateFlow.upHandler
   val backStateList = uiStateFlow.backHandler

   if(uiStateListFlow is UiState.Empty)
      Log.v(tag,"Composition UiStateListFlow UiState.Empty $upStateList $backStateList")
   else if(uiStateListFlow is UiState.Loading)
      Log.v(tag,"Composition UiStateListFlow UiState.Loading $upStateList $backStateList")
   else if(uiStateListFlow is UiState.Success)
      Log.v(tag,"Composition UiStateListFlow UiState.Success $upStateList $backStateList")
   else if(uiStateListFlow is UiState.Error)
      Log.v(tag,"Composition UiStateListFlow UiState.Error $upStateList $backStateList")

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.people_list)) },
            navigationIcon = {
               val activity = LocalContext.current as Activity
               IconButton(
                  onClick = {
                     logDebug(tag, "Lateral Navigation: finish app")
                     // Finish the app
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
            items(
               items = list
            ) { person ->

               var personRemoved: Person?
               val dismissState = rememberDismissState(
                  confirmValueChange = {
                     if (it == DismissValue.DismissedToEnd) {
                        logDebug("==>SwipeToDismiss().", "-> Edit")
                        navController.navigate(NavScreen.PersonDetail.route + "/${person.id}")
                        return@rememberDismissState true
                     } else if (it == DismissValue.DismissedToStart) {
                        logDebug("==>SwipeToDismiss().", "-> Delete")
                        personRemoved = person
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
                     val direction = dismissState.dismissDirection ?: return@SwipeToDismiss

                     val colorBox by animateColorAsState(
                        when (dismissState.targetValue) {
                           DismissValue.Default -> Color.LightGray
                           DismissValue.DismissedToEnd -> Color.Green
                           DismissValue.DismissedToStart -> Color.Red
                        },
                        label = ""
                     )
                     val colorIcon: Color by animateColorAsState(
                        when (dismissState.targetValue) {
                           DismissValue.Default -> Color.Black
                           DismissValue.DismissedToEnd -> Color.DarkGray
                           DismissValue.DismissedToStart -> Color.DarkGray //Color.White
                        },
                        label = ""
                     )
                     val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                     }
                     val icon = when (direction) {
                        DismissDirection.StartToEnd -> Icons.Default.Edit
                        DismissDirection.EndToStart -> Icons.Default.Delete
                     }
                     val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 1.25f else 2.0f,
                        label = ""
                     )

                     Box(
                        Modifier
                           .fillMaxSize()
                           .background(colorBox)
                           .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                     ) {
                        Icon(
                           icon,
                           contentDescription = "Localized description",
                           modifier = Modifier.scale(scale),
                           tint = colorIcon
                        )
                     }
                  },
                  dismissContent = {
                     Column {
                        PersonListCard(
                           id = person.id,
                           firstName = person.firstName,
                           lastName = person.lastName,
                           email = person.email,
                           phone = person.phone,
                           imagePath = person.imagePath ?: "",
                           elevation = CardDefaults.cardElevation(
                              defaultElevation = 4.dp,
                              pressedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
                              focusedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
                              hoveredElevation = 0.dp,
                              draggedElevation = if (dismissState.dismissDirection != null) 8.dp else 0.dp,
                              disabledElevation = 0.dp
                           )
                        ) { id ->
                           // LazyColum item clicked -> DetailScreen initialized
                           logInfo(tag, "Forward Navigation: Item clicked")
                           // Navigate to 'PersonDetail' destination and put 'PeopleList' on the back stack
                           navController.navigate(route = NavScreen.PersonDetail.route + "/$id")
                        }
                     }
                  }
               )
            }
         }
      }
   }
   if (uiStateListFlow is UiState.Error) {
      LaunchedEffect(key1 = uiStateListFlow is UiState.Error)  {
         val message = (uiStateListFlow as UiState.Error).message
         logError(tag, "uiStateListFlow.Error $message")
         showErrorMessage(
            snackbarHostState = snackbarHostState,
            errorMessage = message,
            actionLabel = "Ok",
            onErrorAction = { }
         )
      }
   }

   if (uiStateFlow is UiState.Error) {
      LaunchedEffect(key1 = uiStateFlow is UiState.Error) {
         val message = (uiStateFlow as UiState.Error).message
         logDebug(tag, "uiStateFlow.Error $message")
         showErrorMessage(
            snackbarHostState = snackbarHostState,
            errorMessage = message,
            actionLabel = "Ok",
            onErrorAction = { }
         )
         viewModel.onUiStateFlowChange(UiState.Success(Person()))
      }
   }
}

@Composable
fun PersonListCard(
   id: UUID,
   firstName: String,
   lastName: String,
   email: String?,
   phone: String?,
   imagePath: String?,
   elevation: CardElevation,
   onClick: (UUID) -> Unit    // Event ↑  Person
) {
//12345678901234567890123
   val tag = "ok>PersonListCard     ."

   Card(
      modifier = Modifier
         .fillMaxWidth()
         .clickable {
            logDebug(tag, "Row onClick()")
            onClick(id)  // Event ↑
         },
      elevation = elevation,
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