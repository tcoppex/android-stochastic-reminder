package cc.polysfaer.stochapop.ui.screens.reminder

import android.net.Uri
import android.provider.Settings
import cc.polysfaer.stochapop.data.reminder.Reminder
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.math.min

object ReminderEditSetting {
    const val MAX_TITLE_LENGTH = 48
    const val MAX_MESSAGE_LENGTH = 462
    const val RANDOM_NOTIFICATION_COUNT_LIMIT: Int = 95
    const val DEFAULT_RANDOM_NOTIFICATION_COUNT = 1
    val maxRandomNotificationCount: Int = min(20, RANDOM_NOTIFICATION_COUNT_LIMIT)
}

data class ReminderEditUIState(
    val reminderDetails: ReminderDetails = ReminderDetails(), //
    val initialLoadDone: Boolean = false,
    val previousNotificationCount: Int = 1,
)

// ------------------------------------------------------------------------------------------------

fun getRoundLocalTime(hoursToAdd: Long): LocalTime {
    return LocalTime.of(LocalTime.now().plusHours(hoursToAdd).hour, 0)
}

// TODO? remove ReminderDetails to use Reminder directly, as they are basically the same (minus default values).
data class ReminderDetails(
    val id: Int = 0,

    val title: String = "Reminder",
    val message: String = "",
    val enabled: Boolean = true,
    val useRandomRange: Boolean = true,
    val hasSound: Boolean = true,
    val hasVibration: Boolean = false,
    val soundUri: Uri? = Settings.System.DEFAULT_NOTIFICATION_URI, //
    val notificationCount: Int = ReminderEditSetting.DEFAULT_RANDOM_NOTIFICATION_COUNT,
    val startTime: LocalTime = getRoundLocalTime(1),
    val endTime: LocalTime = getRoundLocalTime(2),
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(), //
)

// ------------------------------------------------------------------------------------------------

fun ReminderDetails.toReminder(): Reminder = Reminder(
    id = id,
    title = title,
    message = message,
    enabled = enabled,
    hasSound = hasSound,
    hasVibration = hasVibration,
    soundUri = soundUri,
    useRandomRange = useRandomRange,
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
    useRandomRange = useRandomRange,
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
