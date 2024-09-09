package de.rogallab.mobile.domain.utilities

fun String.as8(): String =
   this.substring(0..7)+"..."

val UUIDEmpty: String
   get () = "00000000-0000-0000-0000-000000000000"