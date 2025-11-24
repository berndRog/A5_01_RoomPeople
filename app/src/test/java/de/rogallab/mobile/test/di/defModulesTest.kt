package de.rogallab.mobile.test.di

import android.content.Context
import androidx.navigation3.runtime.NavKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.appstorage.AppStorage
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.IAppStorage
import de.rogallab.mobile.domain.IPeopleUcFetchSorted
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.IPersonUseCases
import de.rogallab.mobile.domain.usecases.people.PeopleUcFetchSorted
import de.rogallab.mobile.domain.usecases.person.PersonUcCreate
import de.rogallab.mobile.domain.usecases.person.PersonUcFetchById
import de.rogallab.mobile.domain.usecases.person.PersonUcRemove
import de.rogallab.mobile.domain.usecases.person.PersonUcUpdate
import de.rogallab.mobile.domain.usecases.person.PersonUseCases
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.navigation.PeopleList
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun defModulesTest(
   appHomePath: String,
   ioDispatcher: CoroutineDispatcher
): Module = module {
   val tag = "<-defModulesTest"

   // data modules
   single<CoroutineDispatcher>(named("dispatcherIo")) {
      ioDispatcher
   }

   logInfo(tag, "test single    -> ApplicationProvider.getApplicationContext()")
   single<Context> {
      ApplicationProvider.getApplicationContext()
   }

   logInfo(tag, "test single    -> Seed")
   single<Seed> {
      Seed(
         _context = get<Context>(),
         _isTest = false
      )
   }

   logInfo(tag, "test single    -> AppDatabase")
   single<AppDatabase> {
      Room.inMemoryDatabaseBuilder(
         context = get<Context>(),
         klass = AppDatabase::class.java,
      ).allowMainThreadQueries()
       .build()
   }

   logInfo(tag, "test single    -> IPersonDao")
   single<IPersonDao> {
      get<AppDatabase>().createPersonDao()
   }

   logInfo(tag, "test single    -> SeedDatabase")
   single<SeedDatabase>() {
      SeedDatabase(
         _database = get<AppDatabase>(),
         _personDao = get<IPersonDao>(),
         _seed = get<Seed>()
      )
   }

   logInfo(tag, "test single    -> AppStorage: IAppStorage")
   single<IAppStorage> {
      AppStorage(
         _context = get<Context>(),
         _dispatcher = get(named("dispatcherIo")),
      )
   }

   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _personDao = get<IPersonDao>()
      )
   }

   // domain modules
   // UseCases
   logInfo(tag, "single    -> PeopleUcFetch")
   single<IPeopleUcFetchSorted> {
      PeopleUcFetchSorted(get<IPersonRepository>())
   }

   // single PersonUseCases
   logInfo(tag, "single    -> PersonUcFetchById")
   single { PersonUcFetchById(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcCreate")
   single { PersonUcCreate(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcUpdate")
   single { PersonUcUpdate(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcRemove")
   single { PersonUcRemove(get<IPersonRepository>()) }
   // Aggregation
   logInfo(tag, "single    -> PersonUseCasesc: IPersonUseCases")
   single<IPersonUseCases> {
      PersonUseCases(
         fetchById = get<PersonUcFetchById>(),
         create = get<PersonUcCreate>(),
         update = get<PersonUcUpdate>(),
         remove = get<PersonUcRemove>()
      )
   }


   // ui modules
   logInfo(tag, "test single    -> PersonValidator")
   single<PersonValidator> {
      PersonValidator(
         _context = get<Context>()
      )
   }

   single<INavHandler> {
      Nav3ViewModel(startDestination = PeopleList)
   }

   logInfo(tag, "test viewModel -> Nav3ViewModel as INavHandler (with params)")
   factory { (startDestination: NavKey) ->  // Parameter for startDestination
      Nav3ViewModel(startDestination = startDestination)
   } bind INavHandler::class

   logInfo(tag, "viewModel -> PersonViewModel")
   factory { (navHandler: INavHandler) ->
      PersonViewModel(
         _fetchSorted = get<IPeopleUcFetchSorted>(),
         _personUc = get<IPersonUseCases>(),
         navHandler = navHandler,
         _validator = get<PersonValidator>()
      )
   }
}