package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.dtos.PersonDto
import de.rogallab.mobile.data.mapping.toPerson
import de.rogallab.mobile.data.mapping.toPersonDto
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PersonRepository(
   private val _personDao: IPersonDao
) : IPersonRepository {

   override fun selectAll(): Flow<Result<List<Person>>> =
      _personDao.selectAll()
         .map { personDtos: List<PersonDto> ->
            logDebug(TAG, "selectAll: ${personDtos.size} people")
            personDtos.map { it.toPerson() }
         }
         .map { people -> Result.success(people) }
         .catch { e -> emit(Result.failure(e)) }


   override suspend fun count(): Result<Int> =
      runCatching { _personDao.count() }

   override suspend fun findById(id: String): Result<Person?> =
      runCatching {
         logDebug(TAG, "findById")
         _personDao.findById(id)?.toPerson()
      }

   override suspend fun insert(person: Person): Result<Unit> =
      runCatching {
         logDebug(TAG, "insert ${person.firstName} ${person.lastName}")
         _personDao.insert(person.toPersonDto())
      }

   override suspend fun insert(people: List<Person>): Result<Unit> =
      runCatching {
         logDebug(TAG, "insert(people: ${people.size})")
         _personDao.insert(people.map { it.toPersonDto() })
      }

   override suspend fun update(person: Person): Result<Unit> =
      runCatching {
         logDebug(TAG, "update ${person.firstName} ${person.lastName}")
         _personDao.update(person.toPersonDto())
      }

   override suspend fun remove(person: Person): Result<Unit> =
      runCatching {
         logDebug(TAG, "remove ${person.firstName} ${person.lastName}")
         _personDao.remove(person.toPersonDto())
      }

   companion object {
      private const val TAG = "<-PersonRepository"
   }
}