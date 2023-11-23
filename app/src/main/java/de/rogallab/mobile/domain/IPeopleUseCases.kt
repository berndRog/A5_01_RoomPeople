package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.useCases.people.ReadAll

interface IPeopleUseCases {
   val readAll: ReadAll
}