package cc.polysfaer.stochapop.controller

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.polysfaer.stochapop.MainActivity
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.data.reminder.Reminder

// TODO: change for the notification channel to be created only on first use
//      AND supress unused channels once a week or so.
//
// idea: create the notification channel via the sendNotification method (to handle the "Test" button)
//      and everytime a new reminder is saved, if the number of distinct channel is beyond a threshold,
//      we might destroy the extra channel not in use anymore.
//
object NotificationChannels {
    fun getChannelId(
        isUrgent: Boolean,
        isPublic: Boolean,
        hasSound: Boolean,
        hasVibration: Boolean,
    ): String {
        val importanceStr = if (isUrgent) "URGENT" else "DEFAULT"
        val visibilityStr = if (isPublic) "PUBLIC" else "PRIVATE"
        val soundStr      = if (hasSound) "SOUND" else "NONE"
        val vibrationStr  = if (hasVibration) "VIBRATION" else "NONE"
        val id = "${importanceStr}_${visibilityStr}_${soundStr}_${vibrationStr}"
        return id
    }

    fun getDefaultChannelId(hasSound: Boolean = true, hasVibration: Boolean = false): String = getChannelId(
        isUrgent = true, isPublic = true, hasSound, hasVibration
    )

    fun initialize(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val importances = listOf(NotificationManager.IMPORTANCE_HIGH, NotificationManager.IMPORTANCE_DEFAULT)
        val visibilities = listOf(Notification.VISIBILITY_PUBLIC, Notification.VISIBILITY_PRIVATE)
        val modes = listOf("SV", "S", "V", "0")

        val channels = mutableListOf<NotificationChannel>()

        importances.forEach { importance ->
            val isUrgent = (importance == NotificationManager.IMPORTANCE_HIGH)
            visibilities.forEach { visibility ->
                val isPublic = (visibility == Notification.VISIBILITY_PUBLIC)
                modes.forEach { mode ->
                    val hasSound = mode.first() == 'S'
                    val hasVibration = mode.last() == 'V'
                    val id = getChannelId(isUrgent, isPublic, hasSound, hasVibration)
                    val channel = NotificationChannel(id, "Channel $id", importance).apply {
                        lockscreenVisibility = visibility
                        enableVibration(hasVibration)
                        if (!hasSound) setSound(null, null)
                    }
                    channels.add(channel)
                }
            }
        }
        manager.createNotificationChannels(channels)
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun sendNotification(
    context: Context,
    title: String,
    message: String,
    summaryNotificationId: Int,
    groupKey: String,
    channelId: String = NotificationChannels.getDefaultChannelId(), //
    notificationId: Int = System.currentTimeMillis().toInt(),
    backToActivityOnClick: Boolean = true
) {
    /* Handle click on notification launching the App's activity. */
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        summaryNotificationId,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_reminder_notification)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setGroup(groupKey)
        .setContentIntent(if (backToActivityOnClick) pendingIntent else null)

    val summaryNotification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_reminder_notification)
        .setGroup(groupKey)
        .setGroupSummary(true)
        .setAutoCancel(true)
        .setContentIntent(if (backToActivityOnClick) pendingIntent else null)
        .build()

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notify(notificationId, builder.build())
            notify(summaryNotificationId, summaryNotification)
        }
    }
}

fun sendNotification(
    context: Context,
    reminder: Reminder,
    backToActivityOnClick: Boolean = true
) {
    val summaryNotificationId = reminder.id

    /* Create the specific notification channel when needed. */
    // TODO

    sendNotification(
        context = context,
        title = reminder.title,
        message = reminder.message,
        summaryNotificationId = summaryNotificationId,
        groupKey = "${context.getString(R.string.app_name)}_${summaryNotificationId}",
        // -----------------------------------------------------------------
        channelId = NotificationChannels.getDefaultChannelId(
            hasSound = reminder.hasSound,
            hasVibration = reminder.hasVibration,
        ),
        // -----------------------------------------------------------------
        backToActivityOnClick = backToActivityOnClick
    )
}