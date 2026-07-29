package cc.polysfaer.stochapop.controller.broadcast

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.ReminderScheduleType
import cc.polysfaer.stochapop.data.reminder.ReminderSettings
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max
import kotlin.random.Random


const val INTENT_REMINDER_ID = "REMINDER_ID"
const val INTENT_NOTIFICATION_ID = "NOTIFICATION_ID"


class SchedulerRepository(
    private val context: Context
) {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    /** Program all alarms for a reminder.
     * Called by the main App on Creation / Edit / Reboot. */
    fun scheduleReminderAlarms(reminder: Reminder) {
        assert(reminder.id > 0) { "Invalid reminderId used" }
        assert(reminder.selectedDays.isNotEmpty()) { "No days were selected." }

        if (!reminder.enabled) {
            Log.d(this@SchedulerRepository.javaClass.simpleName, "Reminder ${reminder.id} tried scheduling an alarm while disabled.")
            return
        }

        val useExact = shouldUseExactAlarm(reminder)

        if (reminder.scheduleType.useRange) {
            val localTimeSegment = getTimeSegmentInMinutes(
                startTime = reminder.startTime,
                endTime = reminder.endTime,
                segmentCount = reminder.notificationCount
            )
            for (notificationId in 0 until reminder.notificationCount) {
                val minuteOffset = (notificationId * localTimeSegment).toLong()
                val startTriggerTime = findNextTriggerDateTime(
                    startTime = reminder.startTime,
                    selectedDays = reminder.selectedDays,
                    minuteOffset = minuteOffset
                )
                scheduleRangedNotificationAlarm(
                    reminderId = reminder.id,
                    startTriggerTime = startTriggerTime,
                    localTimeSegment = localTimeSegment,
                    notificationId = notificationId,
                    isRandom = reminder.scheduleType == ReminderScheduleType.RANDOM,
                    useExact = useExact,
                )
            }
        } else {
            val startTriggerTime = findNextTriggerDateTime(
                startTime = reminder.startTime,
                selectedDays = reminder.selectedDays
            )
            scheduleNotificationAlarm(
                reminderId = reminder.id,
                triggerTime = startTriggerTime,
                useExact = useExact
            )
        }
    }

    /** Schedule a single next notification for a reminder, excluding current range.
     * Called by the Alarm Receiver. */
    fun scheduleReminderNextSingleAlarm(reminder: Reminder, notificationId: Int) {
        val startTriggerTime = findNextTriggerDateTime(
            startTime = reminder.startTime,
            selectedDays = reminder.selectedDays
        )

        val useExact = shouldUseExactAlarm(reminder)

        if (reminder.scheduleType.useRange) {
            val localTimeSegment = getTimeSegmentInMinutes(
                startTime = reminder.startTime,
                endTime = reminder.endTime,
                segmentCount = reminder.notificationCount
            )
            scheduleRangedNotificationAlarm(
                reminderId = reminder.id,
                startTriggerTime = startTriggerTime,
                localTimeSegment = localTimeSegment,
                notificationId = notificationId,
                isRandom = reminder.scheduleType == ReminderScheduleType.RANDOM,
                useExact = useExact
            )
        } else {
            scheduleNotificationAlarm(
                reminderId = reminder.id,
                triggerTime = startTriggerTime,
                useExact = useExact
            )
        }
    }

    /** Cancel a single notification alarm. */
    fun cancelNotificationAlarm(reminderId: Int, notificationId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(reminderId, notificationId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.cancel()
    }

    fun cancelNotificationAlarms(
        reminderId: Int,
        firstNotificationId: Int,
        lastNotificationId: Int
    ) {
        for (notificationId in firstNotificationId..< lastNotificationId) {
            cancelNotificationAlarm(reminderId, notificationId)
        }
    }

    /** Set an alarm to trigger a single notification. */
    @SuppressLint("MissingPermission")
    private fun scheduleNotificationAlarm(
        reminderId: Int,
        triggerTime: LocalDateTime,
        notificationId: Int = 0,
        useExact: Boolean = true
    ) {
        val requestCode = getRequestCode(reminderId, notificationId)

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(INTENT_REMINDER_ID, reminderId)
            putExtra(INTENT_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMs = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // ---------------------------------------------------
        /** AlarmManager methods
         * Require Permissions : setAlarmClock, setExact, setExactAndAllowWhileIdle
         * Don't               : setAndAllowWhileIdle, setInexactRepeating, setRepeating, setWindow
         */

        if (useExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        } else {
            // [[setAndAllowWhileIdle is too unprecise, and can wait for ~10minutes or more]]
            // This kind of work with some settings, but often wait for the screen to be opened.
            // Which is ok in 'silent' mode, less so for Exact trigger.
            // If two alarms are close to each others, they might be tight together
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
        Log.d(this@SchedulerRepository.javaClass.simpleName, "Reminder ${reminderId}_$notificationId at $triggerTime")
        // ---------------------------------------------------
    }

    /** Schedule a ranged alarm in a time segment. */
    private fun scheduleRangedNotificationAlarm(
        reminderId: Int,
        startTriggerTime: LocalDateTime,
        localTimeSegment: Double,
        notificationId: Int,
        isRandom: Boolean,
        useExact: Boolean,
    ) {
        val minOffset = (notificationId * localTimeSegment).toLong()
        val maxOffset = ((notificationId + 1) * localTimeSegment).toLong().coerceAtLeast(minOffset + 1)
        val randomOffset = Random.nextLong(minOffset, maxOffset)
        val triggerTime = startTriggerTime.plusMinutes(if (isRandom) randomOffset else minOffset)
        scheduleNotificationAlarm(reminderId, triggerTime, notificationId, useExact)
    }

    /** For random range, return the time segment in minutes in which to sample random value. */
    private fun getTimeSegmentInMinutes(startTime: LocalTime, endTime: LocalTime, segmentCount: Int) : Double {
        val duration = Duration.between(startTime, endTime)
        val rangeMinutes = (if (duration.isNegative) duration.plusDays(1) else duration).toMinutes()
        return rangeMinutes.toDouble() / max(1, segmentCount)
    }

    /** Return the next valid LocalDateTime when to trigger the alarm. */
    private fun findNextTriggerDateTime(
        startTime: LocalTime,
        selectedDays: Set<DayOfWeek>,
        minuteOffset: Long = 0L
    ): LocalDateTime {
        val triggerTime = LocalDate.now().atTime(startTime)
        val now = LocalDateTime.now()
        return generateSequence(triggerTime) { it.plusDays(1) }
            .first {
                   selectedDays.contains(it.dayOfWeek)
                && it.plusMinutes(minuteOffset).isAfter(now)
            }
    }

    /** Return an unique intent request code from a reminderId and a notification index. */
    private fun getRequestCode(reminderId: Int, notificationId: Int) : Int {
        return reminderId * ReminderSettings.RANGED_NOTIFICATION_COUNT_LIMIT + notificationId
    }

    private fun shouldUseExactAlarm(reminder: Reminder): Boolean {
        val useExact = reminder.hasSound || reminder.hasVibration

        // On Android12+ the EXACT_ALARM permission could be revoked. If it's the case we'll use
        // a non exact alarm instead. TODO: alert the user they should give back the permission.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            useExact && alarmManager.canScheduleExactAlarms()
        } else {
            useExact
        }
    }
}