package de.rogallab.mobile.data.local.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UuidConverters {

   @JvmStatic
   @TypeConverter
   fun uuidToString(uuid: Uuid?): String? = uuid?.toString()

   @JvmStatic
   @TypeConverter
   fun stringToUuid(value: String?): Uuid? = value?.let { Uuid.parse(it) }
}

@OptIn(ExperimentalTime::class)
object LocalDateTimeConverters {
   private val utcTimeZone = TimeZone.UTC

   @JvmStatic
   @TypeConverter
   fun localDateTimeToIsoString(dateTime: LocalDateTime?): String? =
      dateTime?.toInstant(utcTimeZone)
         ?.toString() // Convert Instant to ISO-8601 String

   @JvmStatic
   @TypeConverter
   fun isoStringToLocalDateTime(iso: String?): LocalDateTime? =
      iso?.let { Instant.parse(it) } // Parse String to Instant
         ?.toLocalDateTime(utcTimeZone) // Convert Instant to LocalDateTime in UTC

}