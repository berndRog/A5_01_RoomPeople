package de.rogallab.mobile

import android.app.Application
import android.util.Log
import de.rogallab.mobile.data.di.dataModules
import de.rogallab.mobile.domain.di.domainModules
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.di.uiModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AppStart : Application() {
   override fun onCreate() {
      super.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(tag, "onCreate() maxMemory $maxMemory kB")

      Log.i(tag, "Application in onCreate()")
      startKoin {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@AppStart)
         // Load modules
         modules(domainModules, dataModules, uiModules)
      }
   }

   companion object {
      private const val tag = "[AppStart]"
      const val isInfo = true
      const val isDebug = true
      const val isVerbose = true
      const val database_name:    String = "A6_01_RoomPeople.db"
      const val database_version: Int    = 1
      const val URL:              String = "http://10.0.2.2:5000/api/v1.0/"

   }
}