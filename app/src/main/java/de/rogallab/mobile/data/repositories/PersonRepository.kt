package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPersonDao
import de.rogallab.mobile.data.local.dtos.PersonDto
import de.rogallab.mobile.data.mapping.toPerson
import de.rogallab.mobile.data.mapping.toPersonDto
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

class PersonRepository(
   private val _personDao: IPersonDao
) : IPersonRepository {
   override fun getAll(): Flow<Result<List<Person>>> =
      _personDao.selectAll()
         .asResult{ dtos: List<PersonDto> -> dtos.map(PersonDto::toPerson)  }

   override suspend fun findById(id: String): Result<Person?> =
      tryCatching { _personDao.findById(id)?.toPerson()  }

   override suspend fun create(person: Person): Result<Unit> =
      tryCatching { _personDao.insert(person.toPersonDto()) }

   override suspend fun create(people: List<Person>): Result<Unit> =
      tryCatching { _personDao.insert(people.map(Person::toPersonDto)) }

   override suspend fun update(person: Person): Result<Unit> =
      tryCatching { _personDao.update(person.toPersonDto()) }

   override suspend fun remove(person: Person): Result<Unit> =
      tryCatching { _personDao.delete(person.toPersonDto()) }


}