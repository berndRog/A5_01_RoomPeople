package de.rogallab.mobile.domain.entities

import de.rogallab.mobile.domain.utilities.as8
import java.util.UUID

data class Person (
   val firstName: String = "",
   val lastName: String = "",
   val email: String? = null,
   val phone:String? = null,
   val imagePath: String? = null,
   val id: String = UUID.randomUUID().toString()
) {
   fun asString() : String = "$firstName $lastName ${id.as8()}"
}