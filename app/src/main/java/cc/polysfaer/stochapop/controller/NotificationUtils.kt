package cc.polysfaer.stochapop.controller

import android.Manifest
import android.R.attr.visibility
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cc.polysfaer.stochapop.MainActivity
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.ReminderSettings

object NotificationChannels {
    fun hasPostNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission( context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getChannelId(
        isUrgent: Boolean,
        isPublic: Boolean,
        hasSound: Boolean,
        hasVibration: Boolean,
        soundUri: Uri? = ReminderSettings.DEFAULT_NOTIFICATION_URI //
    ): String {
        val importanceStr = if (isUrgent) "URGENT" else "DEFAULT"
        val visibilityStr = if (isPublic) "PUBLIC" else "PRIVATE"
        val soundStr      = if (hasSound) "SOUND" else "NONE"
        val vibrationStr  = if (hasVibration) "VIBRATION" else "NONE"

        val soundHash = if (hasSound && (soundUri != null)) {
            soundUri.toString().hashCode().coerceAtLeast(0)
        } else {
            "NONE"
        }

        val id = "${importanceStr}_${visibilityStr}_${soundStr}_${vibrationStr}_${soundHash}"
        return id
    }

    fun createChannel(
        context: Context,
        isUrgent: Boolean,
        isPublic: Boolean,
        hasSound: Boolean,
        hasVibration: Boolean,
        soundUri: Uri?
    ): String {
        val manager = context.getSystemService(NotificationManager::class.java)

        val channelId = getChannelId(
            isUrgent,
            isPublic,
            hasSound,
            hasVibration,
            soundUri
        )

        val importance = if (isUrgent) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
        val visibility = if (isPublic) Notification.VISIBILITY_PUBLIC else Notification.VISIBILITY_PRIVATE

        val channel = NotificationChannel(channelId, "Channel $channelId", importance).apply {
            lockscreenVisibility = visibility
            enableVibration(hasVibration)
            if (hasSound) {
                setSound(soundUri, null)
            } else {
                setSound(null, null)
            }
        }

        manager.createNotificationChannel(channel)

        return channelId
    }
}

// ------------------------------------------------------------------------------------------------

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
    channelId: String,
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

    /* Create the specific notification channel as needed. */
    // TODO: probably overkill, and don't even garbage collect unused channels.
    val channelId = NotificationChannels.createChannel(
        context = context,
        isUrgent = true, //
        isPublic = true, //
        hasSound = reminder.hasSound,
        hasVibration = reminder.hasVibration,
        soundUri = reminder.soundUri
    )

    sendNotification(
        context = context,
        title = reminder.title,
        message = reminder.message,
        summaryNotificationId = summaryNotificationId,
        groupKey = "${context.getString(R.string.app_name)}_${summaryNotificationId}",
        channelId = channelId,
        backToActivityOnClick = backToActivityOnClick
    )
}