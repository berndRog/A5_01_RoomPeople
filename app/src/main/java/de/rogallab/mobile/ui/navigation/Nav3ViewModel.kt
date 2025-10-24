package de.rogallab.mobile.ui.navigation // Assuming package from other files
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import de.rogallab.mobile.domain.utilities.logDebug
import kotlin.compareTo
import kotlin.text.clear

/**
 * Nav3ViewModel
 *
 * ViewModel for managing a single navigation back stack.
 * Suitable for simple, linear navigation flows without top-level tabs.
 *
 * @param startDestination The initial/root destination
 */
class Nav3ViewModel(
   val startDestination: NavKey = PeopleList
) : ViewModel(), INavHandler {

   init { logDebug(TAG, "init instance=${System.identityHashCode(this)}") }

   // Internal mutable backstack, initialized with the startDestination
   private val _navBackStack: SnapshotStateList<NavKey> =
      mutableStateListOf(startDestination)

   // Public read-only access to backstack
   val backStack: List<NavKey>
      get() = _navBackStack

   // Interface INavHandler implementation
   // ------------------------------------
   /**
    * Pushes a new destination onto the backstack.
    */
   override fun push(destination: NavKey) {
      _navBackStack.add(destination)
      logDebug(TAG, "push: $destination -> Stack: ${_navBackStack.joinToString()}")
   }

   /**
    * Pops the current top destination from the backstack.
    * Prevents removing the root/start destination.
    */
   override fun pop() {
      if (_navBackStack.size > 1) {
         val removedKey = _navBackStack.removeAt(_navBackStack.lastIndex)
         logDebug(TAG, "pop: removed $removedKey -> Stack: ${_navBackStack.joinToString()}")
      } else {
         logDebug(TAG, "pop: at root destination ($startDestination), no action taken")
      }
   }

   /**
    * Clears the backstack and navigates to the root destination.
    */
   override fun popToRootAndNavigate(rootDestination: NavKey) {
      logDebug(TAG, "popToRootAndNavigate to: $rootDestination")
      replaceStack(rootDestination)
   }

   // Internal methods
   // ------------------------------------
   /**
    * Replaces the entire backstack with new destinations.
    * If no keys provided, resets to startDestination.
    */
   private fun replaceStack(vararg keys: NavKey) {
      _navBackStack.clear()
      if (keys.isNotEmpty()) {
         _navBackStack.addAll(keys)
         logDebug(TAG, "replaceStack: ${keys.size} keys -> ${_navBackStack.joinToString()}")
      } else {
         _navBackStack.add(startDestination)
         logDebug(TAG, "replaceStack: empty keys, reset to $startDestination")
      }
   }

   companion object {
      private const val TAG = "<-Nav3ViewModel"
   }
}


//// ViewModel for managing a single navigation back stack.
//// Suitable for simple navigation flows with one top-Levele
//class Nav3ViewModel(
//   val startDestination: NavKey = PeopleList
//) : ViewModel(), INavHandler {
//
//   init { logDebug(TAG, "init instance=${System.identityHashCode(this)}") }
//
//   // Internal mutable backstack. Initialized with the startDestination.
//   private val _navBackStack: SnapshotStateList<NavKey> =
//      mutableStateListOf(startDestination)
//
//   // Public read-only access to backstack
//   val backStack: List<NavKey>
//      get() = _navBackStack
//
//   // Interface INavHandler implementation
//   // ------------------------------------
//   // Pushes a new destination [NavKey] onto the backstack.
//   override fun push(destination: NavKey) {
//      logDebug(TAG, "push: $destination")
//      _navBackStack.add(destination)
//   }
//
//   // Pops the current top destination from the backstack.
//   override fun pop() {
//      if (_navBackStack.size > 1) { // Prevent removing the root/start destination
//         val removedKey = _navBackStack.removeLastOrNull()
//         logDebug(TAG, "pop: $removedKey")
//      } else {
//         logDebug(TAG, "pop: Attempted to remove root destination. No action taken.")
//      }
//   }
//
//   // Pops the current backstack and replaces it with a new stack containing only the [rootDestination].
//   override fun popToRootAndNavigate(rootDestination: NavKey) {
//      logDebug(TAG, "popToRootAndNavigate to: $rootDestination")
//      replaceStack(rootDestination)
//   }
//
//   // Internal methods
//   // ------------------------------------
//   // Replaces the entire backstack with a new set of destinations [NavKey].
//   // If the provided keys array is empty, the stack will be reset to the startDestination.
//   private fun replaceStack(vararg keys: NavKey) {
//      logDebug(TAG, "replaceStack with ${keys.size} keys: ${keys.joinToString()}")
//      _navBackStack.clear()
//      if (keys.isNotEmpty()) {
//         _navBackStack.addAll(keys)
//      } else {
//         _navBackStack.add(startDestination) // Ensure stack is never truly empty
//         logDebug(TAG, "replaceStack: Provided keys were empty, reset to $startDestination")
//      }
//   }
//
//   companion object {
//      private const val TAG = "<-Nav3ViewModel"
//   }
//}