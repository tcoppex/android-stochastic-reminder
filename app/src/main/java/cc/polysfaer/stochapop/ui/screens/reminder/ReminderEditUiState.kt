package cc.polysfaer.stochapop.ui.screens.reminder

import android.net.Uri
import androidx.annotation.StringRes
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.ReminderScheduleType
import cc.polysfaer.stochapop.data.reminder.ReminderSettings
import java.time.DayOfWeek
import java.time.LocalTime

// ------------------------------------------------------------------------------------------------

data class ReminderEditUIState(
    val reminderDetails: ReminderDetails, //
    val initialLoadDone: Boolean = false,
    val previousNotificationCount: Int = ReminderSettings.defaultRandomNotificationCount,
)

// ------------------------------------------------------------------------------------------------

fun getRoundLocalTime(hoursToAdd: Long): LocalTime {
    return LocalTime.of(LocalTime.now().plusHours(hoursToAdd).hour, 0)
}

// TODO? remove ReminderDetails to use Reminder directly, as they are basically the same (minus default values).
data class ReminderDetails(
    val id: Int = 0,

    val title: String,  // a localized default value should be provided on creation..
    val message: String = "",
    val enabled: Boolean = true,
    val hasSound: Boolean = true,
    val hasVibration: Boolean = false,
    val soundUri: Uri? = ReminderSettings.DEFAULT_NOTIFICATION_URI,
    val scheduleType: ReminderScheduleType = ReminderScheduleType.RANDOM,
    val notificationCount: Int = ReminderSettings.defaultRandomNotificationCount,
    val startTime: LocalTime = getRoundLocalTime(1),
    val endTime: LocalTime = getRoundLocalTime(2),
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
)

@StringRes
fun ReminderScheduleType.getLabelResId(): Int = when(this) {
    ReminderScheduleType.FIXED -> R.string.schedule_type_fixed_option
    ReminderScheduleType.RANGED -> R.string.schedule_type_ranged_option
    ReminderScheduleType.RANDOM -> R.string.schedule_type_random_option
}

// ------------------------------------------------------------------------------------------------

fun ReminderDetails.toReminder(): Reminder = Reminder(
    id = id,
    title = title,
    message = message,
    enabled = enabled,
    hasSound = hasSound,
    hasVibration = hasVibration,
    soundUri = soundUri,
    scheduleType = scheduleType,
    notificationCount = notificationCount,
    startTime = startTime,
    endTime = endTime,
    selectedDays = selectedDays,
)

fun Reminder.toReminderDetail(): ReminderDetails = ReminderDetails(
    id = id,
    title = title,
    message = message,
    enabled = enabled,
    hasSound = hasSound,
    hasVibration = hasVibration,
    soundUri = soundUri,
    scheduleType = scheduleType,
    notificationCount = notificationCount,
    startTime = startTime,
    endTime = endTime,
    selectedDays = selectedDays
)

fun Reminder.toReminderEditUIState(initialLoadDone: Boolean = true): ReminderEditUIState = ReminderEditUIState(
    reminderDetails = toReminderDetail(),
    initialLoadDone = initialLoadDone,
    previousNotificationCount = notificationCount,
)

// ------------------------------------------------------------------------------------------------
