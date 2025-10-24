package de.rogallab.mobile.ui

//import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import de.rogallab.mobile.domain.utilities.logComp
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.images.ImageViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.navigation.PeopleList
import de.rogallab.mobile.ui.navigation.composables.AppNavigation
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : BaseActivity(TAG) {

   // lazy initialization of the ViewModel with koin
   // Activity-scoped ViewModels viewModelStoreOwner = MainActivity
   private val _navViewModel: Nav3ViewModel by viewModel {
      parametersOf(PeopleList) }

   private val _personViewModel: PersonViewModel by viewModel{
      parametersOf(_navViewModel as INavHandler) }
   private val _imageViewModel: ImageViewModel by viewModel{
      parametersOf(_navViewModel as INavHandler) }


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      logDebug(TAG, "_navViewModel=${System.identityHashCode(_navViewModel)}")
      logDebug(TAG, "_peopleViewModel=${System.identityHashCode(_personViewModel)}")

      enableEdgeToEdge()

      setContent {

         val nComp = remember { mutableIntStateOf(1) }
         SideEffect { logComp(TAG, "Composition #${nComp.value++}") }


         AppTheme {
            AppNavigation(
               // startDestination = PeopleList
               navViewModel = _navViewModel,
               personViewModel = _personViewModel,
               imageViewModel = _imageViewModel
            )
         }
      }

   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}
