package de.rogallab.mobile.domain.usecases.person

import de.rogallab.mobile.domain.IPersonUseCases

data class PersonUseCases(
   override val fetchById: PersonUcFetchById,
   override val insert: PersonUcInsert,
   override val update: PersonUcUpdate,
   override val remove: PersonUcRemove
): IPersonUseCases