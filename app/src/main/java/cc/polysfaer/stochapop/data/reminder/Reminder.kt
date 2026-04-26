package cc.polysfaer.stochapop.data.reminder

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                                        // Switch to Long?
    val title: String,                                      // size = ReminderEditUiState.MAX_TITLE_LENGTH
    val message: String,                                    // size = ReminderEditUiState.MAX_MESSAGE_LENGTH
    val enabled: Boolean,
    val useRandomRange: Boolean,
    val hasSound: Boolean,
    val hasVibration: Boolean,
    val soundUri: Uri?,                                     // stored as 'String?'
    val notificationCount: Int,
    val startTime: LocalTime,                               // stored as Int (minutes of the day)
    val endTime: LocalTime,                                 // stored as Int (minutes of the day)
    val selectedDays: Set<DayOfWeek>,                       // stored as Int (mask of days)
)

class ReminderTimeConverter {
    @TypeConverter
    fun fromMinutesOfDay(value: Int?): LocalTime? {
        return value?.let {
            LocalTime.of(it / 60, it % 60)
        }
    }

    @TypeConverter
    fun toMinutesOfDay(time: LocalTime?): Int? {
        return time?.let {
            (it.hour * 60) + it.minute
        }
    }
}

class ReminderDayConverter {
    @TypeConverter
    fun fromSelectedDayBitmask(mask: Int?): Set<DayOfWeek>? {
        return mask?.let {
            DayOfWeek.entries.filterIndexed  { index, _ ->
                mask and (1 shl index) != 0
            }.toSet()
        }
    }

    @TypeConverter
    fun toSelectedDayBitmask(selectedDays: Set<DayOfWeek>?): Int? {
        return selectedDays?.let {
            selectedDays.fold(0) { curr, day ->
                curr or (1 shl (DayOfWeek.entries.indexOf(day)))
            }
        }
    }
}

class ReminderUriConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}
