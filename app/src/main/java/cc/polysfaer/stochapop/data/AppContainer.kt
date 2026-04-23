package cc.polysfaer.stochapop.data

import android.content.Context
import android.content.SharedPreferences
import cc.polysfaer.stochapop.controller.broadcast.SchedulerRepository
import cc.polysfaer.stochapop.data.reminder.OfflineRemindersRepository
import cc.polysfaer.stochapop.data.reminder.RemindersRepository

interface AppContainer {
    val prefs: SharedPreferences

    /* Manage the Database storing Reminders. */
    val remindersRepository: RemindersRepository

    /* Manage the alarms scheduler. */
    val schedulerRepository: SchedulerRepository
}

enum class AppSettingsName(val key: String) {
    IS_FIRST_RUN("is_first_run")
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    override val remindersRepository: RemindersRepository by lazy {
        OfflineRemindersRepository(AppDatabase.getDatabase(context).reminderDao())
    }

    override val schedulerRepository = SchedulerRepository(context)
}
