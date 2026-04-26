package cc.polysfaer.stochapop.ui.screens.home

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.polysfaer.stochapop.controller.broadcast.SchedulerRepository
import cc.polysfaer.stochapop.data.AppSettingsName
import cc.polysfaer.stochapop.data.DataSource
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.RemindersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val reminderList: List<Reminder> = listOf()
)

/* ViewModel to retrieve all items in the Room Database. */
class HomeViewModel(
    private val application: Application,
    private val remindersRepository: RemindersRepository,
    private val schedulerRepository: SchedulerRepository,
    private val sharedPreference: SharedPreferences
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = remindersRepository
        .getAllRemindersStream()
        .map { HomeUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )

    init {
        initializeOnFirstRun()
    }

    private fun initializeOnFirstRun() {
        val isFirstRun = sharedPreference.getBoolean(AppSettingsName.IS_FIRST_RUN.key, true)

        val reminderList = DataSource.getReminders(application)

        if (isFirstRun) {
            viewModelScope.launch {
                remindersRepository.insertReminders(reminderList)
                reminderList.filter { it.enabled }.forEach {
                    schedulerRepository.scheduleReminderAlarms(it)
                }
            }
            sharedPreference.edit { putBoolean(AppSettingsName.IS_FIRST_RUN.key, false) }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        val newReminder = reminder.copy(enabled = !reminder.enabled)

        viewModelScope.launch {
            if (newReminder.enabled) {
                schedulerRepository.scheduleReminderAlarms(newReminder)
            } else {
                schedulerRepository.cancelNotificationAlarms(
                    newReminder.id,
                    0,
                    newReminder.notificationCount
                )
            }

            remindersRepository.updateReminder(newReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            schedulerRepository.cancelNotificationAlarms(
                reminder.id,
                0,
                reminder.notificationCount
            )
            remindersRepository.deleteReminder(reminder)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}