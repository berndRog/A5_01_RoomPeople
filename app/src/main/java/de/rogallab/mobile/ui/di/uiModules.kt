package de.rogallab.mobile.ui.di

import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.errors.ErrorResources
import de.rogallab.mobile.ui.errors.ResourceProvider
import de.rogallab.mobile.ui.people.PeopleViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val uiModules: Module = module {

   // Application and Context are implicitly available
   logInfo("[Koin]", "single -> ResourceProvider")
   singleOf(::ResourceProvider)

   logInfo("[Koin]", "single -> ErrorResources")
   singleOf(::ErrorResources)

   logInfo("[Koin]", "viewModelOf -> PeopleViewModel")
   viewModelOf(::PeopleViewModel)    // no parameter needed for viewModelOf


}