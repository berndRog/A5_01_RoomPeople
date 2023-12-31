package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IPeopleDao
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class PeopleRepositoryImpl @Inject constructor(
   private val _peopleDao: IPeopleDao,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
): IPeopleRepository {

   val tag = "ok>PeopleRepositoryImpl"

   override fun selectAll(): Flow<List<PersonDto>> {
      // throw Exception("Error thrown in selectAll()")
      logDebug(tag,"suspend selectAll()")
      return _peopleDao.selectAll()
   }

   override suspend fun findById(id: UUID): PersonDto? =
      withContext(_dispatcher+_exceptionHandler) {
         val personDto =_peopleDao.selectById(id)
         logDebug(tag,"suspend findById() ${personDto?.asString()}")
//       throw Exception("Error thrown in findById()")
         return@withContext personDto
      }

   override suspend fun count(): Int =
      withContext(_dispatcher+_exceptionHandler) {
         val records =_peopleDao.count()
         logDebug(tag,"suspend count() $records")
//       throw Exception("Error thrown in count()")
         return@withContext records
      }

   override suspend fun add(personDto: PersonDto): Boolean =
      withContext(_dispatcher+_exceptionHandler) {
//       throw Exception("Error thrown in add()")
         _peopleDao.insert(personDto)
         logDebug(tag,"suspend insert() ${personDto.asString()}")
         return@withContext true
      }

   override suspend fun addAll(peopleDtos: List<PersonDto>): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in addAll()")
         _peopleDao.insertAll(peopleDtos)
         logDebug(tag, "suspend addAll()")
         return@withContext true
      }

   override suspend fun update(personDto: PersonDto): Boolean =
      withContext(_dispatcher+_exceptionHandler) {
//       throw Exception("Error thrown in update()")
         _peopleDao.update(personDto)
         logDebug(tag,"suspend update()")
         return@withContext true
      }

   override suspend fun remove(personDto: PersonDto): Boolean =
      withContext(_dispatcher + _exceptionHandler) {
//       throw Exception("Error thrown in remove()")
         _peopleDao.delete(personDto)
         logDebug(tag, "suspend remove()")
         return@withContext true
      }
}