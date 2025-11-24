package de.rogallab.mobile

import androidx.compose.material3.SnackbarDuration

object Globals {

   const val databaseName = "people51.db"
   const val databaseVersion  = 1


   const val mediaStoreGroupname = "Photos Room 51"
   val directoryName = "android"
   val fileName = "people51"


   val animationDuration = 1000
   val snackbarDuration = SnackbarDuration.Indefinite

   var isDebug = true
   var isInfo = true
   var isVerbose = true
   var isComp = false
}