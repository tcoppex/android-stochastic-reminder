package cc.polysfaer.stochapop.data.reminder

import kotlinx.coroutines.flow.Flow

class OfflineRemindersRepository(private val reminderDao: ReminderDao): RemindersRepository {
    override fun getAllRemindersStream(): Flow<List<Reminder>> = reminderDao.getAllRemindersStream()

    override fun getReminderStream(id: Int): Flow<Reminder?> = reminderDao.getReminderStream(id)

    override suspend fun getAllReminders(): List<Reminder> = reminderDao.getAllReminders()

    override suspend fun getReminder(id: Int): Reminder? = reminderDao.getReminder(id)

    override suspend fun insertReminders(reminders: List<Reminder>) = reminderDao.insertAll(reminders)

    override suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)

    override suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)

    override suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
}