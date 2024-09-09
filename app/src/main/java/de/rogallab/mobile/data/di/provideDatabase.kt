package de.rogallab.mobile.data.di

import android.app.Application
import androidx.room.Room
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.local.database.AppDatabase

fun provideAppDatabase(application: Application): AppDatabase =
   Room.databaseBuilder(
      application,
      AppDatabase::class.java,
      AppStart.database_name
   ).
   fallbackToDestructiveMigration().build()

fun providePeopleDao(appDataBase: AppDatabase): IPeopleDao =
   appDataBase.createPeopleDao()
