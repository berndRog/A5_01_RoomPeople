package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.dto.PersonDto
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.mapping.toPerson
import de.rogallab.mobile.domain.mapping.toPersonDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class PeopleRepository(
   private val _peopleDao: IPeopleDao,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
): IPeopleRepository {

   override fun getAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         _peopleDao.selectAll().collect { it: List<PersonDto> ->
            val people = it.map { it.toPerson() }
            emit(ResultData.Success(people))
         }
      } catch (t: Throwable) {
         ResultData.Failure(t)
      }
   }

   override suspend fun findById(id: String): ResultData<Person?> =
      withContext(_dispatcher+_exceptionHandler) {
         try {
            val person = _peopleDao.selectById(id)?.toPerson()
            return@withContext ResultData.Success(person)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun count(): Int =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext _peopleDao.count()
      }

   override suspend fun create(person: Person): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         try {
            val personDto = person.toPersonDto()
            _peopleDao.insert(personDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun createAll(people: List<Person>): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         try {
            val peopleDto = people.map { it: Person -> it.toPersonDto() }
            _peopleDao.insertAll(peopleDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun update(person: Person): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         try {
            val personDto = person.toPersonDto()
            _peopleDao.update(personDto)
            return@withContext ResultData.Success(Unit)
         } catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun remove(person: Person): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
          try {
             val personDto = person.toPersonDto()
             _peopleDao.delete(personDto)
             return@withContext ResultData.Success(Unit)
          } catch (t: Throwable) {
             return@withContext ResultData.Failure(t)
          }
      }
}