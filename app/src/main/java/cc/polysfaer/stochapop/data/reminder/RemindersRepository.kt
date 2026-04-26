package cc.polysfaer.stochapop.data.reminder

import kotlinx.coroutines.flow.Flow

interface RemindersRepository {
    fun getAllRemindersStream(): Flow<List<Reminder>>
    fun getReminderStream(id: Int): Flow<Reminder?>
    suspend fun getAllReminders(): List<Reminder>
    suspend fun getReminder(id: Int): Reminder?
    suspend fun insertReminders(reminders: List<Reminder>)
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun updateReminder(reminder: Reminder)
}

