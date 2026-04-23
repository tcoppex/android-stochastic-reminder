package cc.polysfaer.stochapop.data

import android.content.Context
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.ui.screens.reminder.ReminderDetails
import cc.polysfaer.stochapop.ui.screens.reminder.toReminder
import java.time.DayOfWeek
import java.time.LocalTime

object DataSource {

    fun getReminders(context: Context): List<Reminder> {
        return listOf(
            ReminderDetails(
                id = 1,
                title = context.getString(R.string.tuto_1_title),
                message = context.getString(R.string.tuto_1_message),
                enabled = false,
                useRandomRange = false,
                hasSound = true,
                hasVibration = false,
                selectedDays = setOf(DayOfWeek.MONDAY)
            ),
            ReminderDetails(
                id = 2,
                title = context.getString(R.string.tuto_2_title),
                message = context.getString(R.string.tuto_2_message),
                enabled = false,
                hasSound = false,
                hasVibration = true,
                notificationCount = 5,
                selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                startTime = LocalTime.of(23, 30),
                endTime = LocalTime.of(0, 30)
            ),
            ReminderDetails(
                id = 3,
                title = context.getString(R.string.tuto_3_title),
                message = "${context.getString(R.string.tuto_3_message)} \uD83C\uDF3F \uD83E\uDDA5",
                enabled = false,
                hasSound = false,
                hasVibration = false,
                notificationCount = 12,
                selectedDays = setOf(DayOfWeek.SATURDAY)
            ),
            ReminderDetails(
                id = 4,
                title = context.getString(R.string.tuto_4_title),
                message = "◝(ᵔᗜᵔ)◜ ♡ ",
                enabled = true,
                useRandomRange = false,
                hasSound = true,
                hasVibration = true,
                startTime = LocalTime.now().plusMinutes(1)
            ),
        ).map { it.toReminder() }
    }
}
