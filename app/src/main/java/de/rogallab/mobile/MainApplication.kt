package de.rogallab.mobile

import android.app.Application
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.di.appModules
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {

   private val _appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

   override fun onCreate() {
      super.onCreate()
      logInfo(TAG, "onCreate()")

      // Initialize Koin dependency injection
      logInfo(TAG, "onCreate(): startKoin{...}")
      startKoin {
         androidLogger(Level.DEBUG)
         // Reference to Android context
         androidContext(androidContext = this@MainApplication)
         // Load modules
         modules(appModules)
      }

      val seedDatabase: SeedDatabase = get<SeedDatabase>()
      _appScope.launch {
         seedDatabase.seedPerson()
      }
   }

   companion object {
      private const val TAG = "<-MainApplication"
   }

}
