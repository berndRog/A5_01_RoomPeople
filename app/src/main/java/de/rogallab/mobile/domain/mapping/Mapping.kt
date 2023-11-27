package de.rogallab.mobile.domain.mapping

import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.domain.entities.Person

fun PersonDto.toPerson(): Person = Person(
   firstName = this.firstName,
   lastName = this.lastName,
   email = this.email,
   phone = this.phone,
   imagePath = this.imagePath,
   id = this.id
)

fun List<PersonDto>.toPerson(): List<Person> {
   val people = mutableListOf<Person>()
   this.forEach { personDto ->
      people.add(personDto.toPerson())
   }
   return people
}

fun Person.toPersonDto(): PersonDto = PersonDto(
   firstName = this.firstName,
   lastName = this.lastName,
   email = this.email,
   phone = this.phone,
   imagePath = this.imagePath,
   id = this.id
)

fun List<Person>.toPersonDto(): List<PersonDto> {
   val peopleDto = mutableListOf<PersonDto>()
   this.forEach { person ->
      peopleDto.add(person.toPersonDto())
   }
   return peopleDto
}