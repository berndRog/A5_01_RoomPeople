package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface IPersonRepository {
   fun selectAll(): Flow<Result<List<Person>>>
   suspend fun count(): Result<Int>
   suspend fun findById(id: String): Result<Person?>

   suspend fun insert(person: Person): Result<Unit>
   suspend fun insert(people: List<Person>): Result<Unit>
   suspend fun update(person: Person): Result<Unit>
   suspend fun remove(person: Person): Result<Unit>
}