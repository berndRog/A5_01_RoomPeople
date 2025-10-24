package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleUcFetch
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

class PeopleUcFetch(
   private val _repository: IPersonRepository
): IPeopleUcFetch {
   // preserves the reactive nature of Flow, allowing continuous updates
   // from the repository if the data changes.
   override operator fun invoke(): Flow<Result<List<Person>>> =
      _repository.selectAll()
}