package de.rogallab.mobile.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.AppNavHost
import de.rogallab.mobile.ui.theme.AppTheme

class MainActivity : BaseActivity(TAG) {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         AppTheme {
            Surface{
               AppNavHost()
            }
         }
      }
   }

   companion object {
      private const val TAG = "[MainActivity]"
   }
}


//
//@Composable
//fun Greeting(name: String) {
//   Text(text = "Hello $name!")
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//   B4_00_FlowTheme {
//      Greeting("Android")
//   }
//}