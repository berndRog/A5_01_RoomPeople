package de.rogallab.mobile.data

import androidx.room.*
import de.rogallab.mobile.data.dto.PersonDto
import kotlinx.coroutines.flow.Flow

@Dao
interface IPeopleDao {
   // QUERIES ---------------------------------------------
   @Query("SELECT * FROM people")
   fun selectAll(): Flow<List<PersonDto>>               // Observable Read
   @Query("SELECT * FROM people WHERE id = :id")     // One-Shot Read
   suspend fun selectById(id: String): PersonDto?
   @Query("SELECT COUNT(*) FROM people")
   suspend fun count(): Int                             // One-shot read

   // COMMANDS --------------------------------------------
   @Insert(onConflict = OnConflictStrategy.ABORT)       // One-Shot Write
   suspend fun insert(personDto: PersonDto)
   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insertAll(peopleDto: List<PersonDto>)    // One-shot write
   @Update
   suspend fun update(personDto: PersonDto)             // One-Shot Write
   @Delete
   suspend fun delete(personDto: PersonDto)             // One-Shot Write
   @Query("DELETE FROM people")
   suspend fun deleteAll()
}