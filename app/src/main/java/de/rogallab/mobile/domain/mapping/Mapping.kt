package de.rogallab.mobile.domain.mapping

import de.rogallab.mobile.data.dto.PersonDto
import de.rogallab.mobile.domain.entities.Person

fun PersonDto.toPerson(): Person = Person(
   firstName = this.firstName,
   lastName = this.lastName,
   email = this.email,
   phone = this.phone,
   imagePath = this.imagePath,
   id = this.id
)

fun Person.toPersonDto(): PersonDto = PersonDto(
   firstName = this.firstName,
   lastName = this.lastName,
   email = this.email,
   phone = this.phone,
   imagePath = this.imagePath,
   id = this.id
)
