package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface IPeopleRepository {
   fun getAll(): Flow<ResultData<List<Person>>>
   suspend fun findById(id: String): ResultData<Person?>
   suspend fun count(): Int
   suspend fun create(person: Person): ResultData<Unit>
   suspend fun createAll(people: List<Person>): ResultData<Unit>
   suspend fun update(person: Person): ResultData<Unit>
   suspend fun remove(person: Person): ResultData<Unit>
}