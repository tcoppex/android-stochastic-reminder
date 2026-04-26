package cc.polysfaer.stochapop.data.reminder

import android.net.Uri
import android.provider.Settings
import kotlin.math.min

object ReminderSettings {
    /* Those values should never be changed. */

    const val MAX_TITLE_LENGTH = 48
    const val MAX_MESSAGE_LENGTH = 462
    const val RANDOM_NOTIFICATION_COUNT_LIMIT: Int = 95

    val DEFAULT_NOTIFICATION_URI: Uri = Settings.System.DEFAULT_NOTIFICATION_URI

    // -----

    /* Those values could be changed later on by the user. */

    val maxRandomNotificationCount: Int = min(20, RANDOM_NOTIFICATION_COUNT_LIMIT)

    val defaultRandomNotificationCount = min(1, maxRandomNotificationCount)
}