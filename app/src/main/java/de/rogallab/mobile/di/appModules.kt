package de.rogallab.mobile.di

import androidx.navigation3.runtime.NavKey
import androidx.room.Room
import de.rogallab.mobile.Globals.DATABASE_NAME
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.appstorage.AppStorage
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.mediastore.MediaStore
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.*
import de.rogallab.mobile.domain.usecases.images.ImageUcCaptureCam
import de.rogallab.mobile.domain.usecases.images.ImageUcSelectGal
import de.rogallab.mobile.domain.usecases.images.ImageUseCases
import de.rogallab.mobile.domain.usecases.people.PeopleUcFetch
import de.rogallab.mobile.domain.usecases.person.*
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.images.ImageViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val defModules: Module = module {
   val tag = "<-defModules"

   // region coroutine dispatchers
   // Provide Dispatchers
   logInfo(tag, "single    -> MainDispatcher:CoroutineDispatcher")
   single<CoroutineDispatcher>(named("MainDispatcher")) { Dispatchers.Main }
   logInfo(tag, "single    -> IODispatcher:CoroutineDispatcher")
   single<CoroutineDispatcher>(named("IODispatcher")) { Dispatchers.IO }
   logInfo(tag, "single    -> DefaultDispatcher:CoroutineDispatcher")
   single<CoroutineDispatcher>(named("DefaultDispatcher")) { Dispatchers.Default }
   // endregion

   // region data modules
   logInfo(tag, "single    -> AppStorage:IAppStorage")
   single<IAppStorage> {
      AppStorage(
         _context = androidContext(),
         _ioDispatcher = get<CoroutineDispatcher>(named("IODispatcher"))
      )
   }

   logInfo(tag, "single    -> MediaStore:IMediaStore")
   single<IMediaStore> {
      MediaStore(
         _context = androidContext(),
         _ioDispatcher = get<CoroutineDispatcher>(named("IODispatcher"))
      )
   }

   logInfo(tag, "single    -> AppDatabase")
   single<AppDatabase> {
      Room.databaseBuilder(
         context = androidContext(),
         klass = AppDatabase::class.java,
         name = DATABASE_NAME
      ).build()
   }

   logInfo(tag, "single    -> IPersonDao")
   single<IPersonDao> {
      get<AppDatabase>().createPersonDao()
   }

   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _personDao = get<IPersonDao>()
      )
   }

   logInfo(tag, "single    -> Seed")
   single<Seed> {
      Seed(
         _context = androidContext(),
         _appStorage = get<IAppStorage>(),
      )
   }

   logInfo(tag, "single    -> SeedDatabase")
   single<SeedDatabase> {
      SeedDatabase(
         _database = get<AppDatabase>(),
         _personDao = get<IPersonDao>(),
         _seed = get<Seed>()
      )
   }
   // endregion

   // region domain modules
   // UseCases
   logInfo(tag, "single    -> PeopleUcFetchSorted: IPeopleUcFetchSorted")
   single<IPeopleUcFetch> {
      PeopleUcFetch(get<IPersonRepository>())
   }

   // single PersonUseCases
   logInfo(tag, "single    -> PersonUcFetchById")
   single { PersonUcFetchById(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcCreate")
   single { PersonUcInsert(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcUpdate")
   single { PersonUcUpdate(get<IPersonRepository>()) }
   logInfo(tag, "single    -> PersonUcRemove")
   single { PersonUcRemove(get<IPersonRepository>()) }
   // Aggregation
   logInfo(tag, "single    -> PersonUseCasesc: IPersonUseCases")
   single<IPersonUseCases> {
      PersonUseCases(
         fetchById = get<PersonUcFetchById>(),
         insert = get<PersonUcInsert>(),
         update = get<PersonUcUpdate>(),
         remove = get<PersonUcRemove>()
      )
   }

   // single ImageUseCases
   logInfo(tag, "single    -> ImagesUcCapture")
   single { ImageUcCaptureCam(get<IMediaStore>()) }
   logInfo(tag, "single    -> ImageUcSelectFromGallery")
   single { ImageUcSelectGal(get<IMediaStore>()) }

   // Aggregation
   single<IImageUseCases> {
      ImageUseCases(
         captureImage = get<ImageUcCaptureCam>(),
         selectImage = get<ImageUcSelectGal>(),
      )
   }
   // endregion

   //region  ui modules
   logInfo(tag, "single    -> PersonValidator")
   single {
      PersonValidator(androidContext())
   }

   logInfo(tag, "viewModel -> Nav3ViewModel as INavHandler (with params)")
   viewModel { (startDestination: NavKey) ->  // Parameter for startDestination
      Nav3ViewModel(startDestination = startDestination)
   } bind INavHandler::class

   logInfo(tag, "viewModel -> ImageViewModel")
   viewModel { (navHandler: INavHandler) ->
      ImageViewModel(
         _imageUc = get<IImageUseCases>(),
         navHandler = navHandler,
      )
   }

   logInfo(tag, "viewModel -> PersonViewModel")
   viewModel { (navHandler: INavHandler) ->
      PersonViewModel(
         _fetchSorted = get<IPeopleUcFetch>(),
         _personUc = get<IPersonUseCases>(),
         // _repository = get<IPersonRepository>(),
         navHandler = navHandler,
         _validator = get<PersonValidator>()
         //_ioDispatcher = get<CoroutineDispatcher>(named("IODispatcher"))
      )
   }
   // endregion
}

val appModules: Module = module {

   try {
      val testedModules = defModules
      requireNotNull(testedModules) {
         "definedModules is null"
      }
      includes(
         testedModules,
         //useCaseModules
      )
   }
   catch (e: Exception) {
      logInfo("<-appModules", e.message!!)
   }
}
