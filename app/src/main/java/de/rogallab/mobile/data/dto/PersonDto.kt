package de.rogallab.mobile.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rogallab.mobile.domain.utilities.as8
import java.util.UUID

@Entity(tableName = "people")
data class PersonDto (
   val firstName: String = "",
   val lastName: String = "",
   val email: String? = null,
   val phone:String? = null,
   val imagePath: String? = null,
   @PrimaryKey
   val id: String = UUID.randomUUID().toString()
) {
   fun asString() : String = "$firstName $lastName ${id.as8()}"
}