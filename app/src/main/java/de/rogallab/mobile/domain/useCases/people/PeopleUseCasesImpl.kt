package de.rogallab.mobile.domain.useCases.people

import de.rogallab.mobile.domain.IPeopleUseCases
import javax.inject.Inject

data class PeopleUseCasesImpl @Inject constructor(
   override val readAll: ReadAll
) : IPeopleUseCases