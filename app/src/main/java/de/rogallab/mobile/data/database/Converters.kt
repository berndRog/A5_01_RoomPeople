package de.rogallab.mobile.data.database
import androidx.room.TypeConverter
import de.rogallab.mobile.domain.utilities.formatISO
import de.rogallab.mobile.domain.utilities.systemZoneId
import de.rogallab.mobile.domain.utilities.toZonedDateTimeUTC
import java.time.ZonedDateTime

object ZonedDateTimeConverters {

   @TypeConverter
   @JvmStatic
   fun toZonedDateTime(zulu: String?): ZonedDateTime? =
      zulu?.let {
         ZonedDateTime.parse(zulu, formatISO).withZoneSameInstant(systemZoneId)
      }

   @TypeConverter
   @JvmStatic
   fun fromZonedDateTime(zdt: ZonedDateTime?): String?  =
       zdt?.let {
          toZonedDateTimeUTC(zdt).format(formatISO)
       }
}