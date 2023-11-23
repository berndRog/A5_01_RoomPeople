package de.rogallab.mobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import de.rogallab.mobile.data.repositories.PeopleRepositoryImpl
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.useCases.people.PeopleUseCasesImpl
import javax.inject.Singleton

// @Binds Shothand for binding an interface type

@InstallIn(ViewModelComponent::class)
@Module
abstract class ABindViewModelModules {
   @ViewModelScoped
   @Binds
   abstract fun bindPeopleUseCases(
      peopleUseCases: PeopleUseCasesImpl
   ): IPeopleUseCases
}

@Module
@InstallIn(SingletonComponent::class)
interface IBindSingletonModules {
   @Binds
   @Singleton
   fun bindWorkOrdersRepository(
      peopleRepositoryImpl: PeopleRepositoryImpl
   ): IPeopleRepository
}