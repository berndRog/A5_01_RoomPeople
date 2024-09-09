package de.rogallab.mobile.data.di


import de.rogallab.mobile.data.repositories.PeopleRepository
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.utilities.logInfo
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModules = module {

   logInfo("[Koin]", "single -> AppDatabase")
   single { provideAppDatabase(androidApplication()) }

   logInfo("[Koin]", "single -> PeopleDao")
   single { providePeopleDao(get()) }

   logInfo("[Koin]", "singleOf -> PeopleRepository")
   singleOf(::PeopleRepository)  { bind<IPeopleRepository>()  }

}