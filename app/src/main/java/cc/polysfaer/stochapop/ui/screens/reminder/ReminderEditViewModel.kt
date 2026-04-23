package cc.polysfaer.stochapop.ui.screens.reminder

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.controller.broadcast.SchedulerRepository
import cc.polysfaer.stochapop.data.reminder.RemindersRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * ViewModel to handle both the NewReminder and EditReminder Screen.
 **/
class ReminderEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val remindersRepository: RemindersRepository,
    private val schedulerRepository: SchedulerRepository,
) : ViewModel() {
    val reminderId: Int = savedStateHandle[EditReminderDestination.REMINDER_ID_ARG] ?: -1
    val isEditMode: Boolean = reminderId != -1

    var uiState by mutableStateOf(ReminderEditUIState(
        reminderDetails = ReminderDetails(title = application.getString(R.string.edit_reminder_default_title)),
        initialLoadDone = !isEditMode
    ))  // switch to FlowState?
        private set

    init {
        /* [EditMode] Retrieve the reminder matching the id. */
        if (isEditMode) {
            viewModelScope.launch  {
                uiState = remindersRepository.getReminderStream(reminderId)
                    .filterNotNull()
                    .first()
                    .toReminderEditUIState(initialLoadDone = true)
            }
        }
    }

    // -----------------------------

    private fun updateReminderDetails(reducer: (ReminderDetails) -> ReminderDetails) {
        uiState = uiState.copy(reminderDetails = reducer(uiState.reminderDetails))
    }

    fun setTitle(title: String) = updateReminderDetails {
        it.copy(title = title.take(ReminderEditSetting.MAX_TITLE_LENGTH).replace("\n", ""))
    }

    fun setMessage(message: String) = updateReminderDetails {
        it.copy(message = message.take(ReminderEditSetting.MAX_MESSAGE_LENGTH))
    }

    fun toggleEnable(state: Boolean) = updateReminderDetails {
        it.copy(enabled = state)
    }

    fun toggleRandomRange(state: Boolean) = updateReminderDetails {
        it.copy(useRandomRange = state)
    }

    fun toggleSound(state: Boolean) = updateReminderDetails {
        it.copy(hasSound = state)
    }

    fun toggleVibration(state: Boolean) = updateReminderDetails {
        it.copy(hasVibration = state)
    }

    fun setStartTime(startTime: LocalTime) = updateReminderDetails {
        it.copy(startTime = startTime)
    }

    fun setTimeRangeEnd(endTime: LocalTime) = updateReminderDetails {
        it.copy(endTime = endTime)
    }

    fun setRangeNotificationCount(count: Int) = updateReminderDetails {
        it.copy(notificationCount = count)
    }

    fun setSelectedDays(selectedDays: Set<DayOfWeek>) = updateReminderDetails {
        if (selectedDays.isEmpty()) {
            it
        } else {
            it.copy(selectedDays = selectedDays)
        }
    }

    // -----------------------------

    suspend fun saveReminder() {
        val reminder = uiState.reminderDetails.toReminder()

        // When we create a new reminder we do not have their primary key and have to get it from 'insert'.
        val reminderId: Int =  if (isEditMode) {
            remindersRepository.updateReminder(reminder)
            reminder.id
        } else {
            remindersRepository.insertReminder(reminder).toInt()
        }

        // In Edit Mode we don't need to cancel the previous alarms as they will be replaced,
        // except when the notification count decreased.
        schedulerRepository.cancelNotificationAlarms(
            reminderId,
            reminder.notificationCount,
            uiState.previousNotificationCount
        )

        // We need to change the reminder.id as it is not defined when first creating it.
        schedulerRepository.scheduleReminderAlarms(reminder.copy(id = reminderId))
    }

    suspend fun deleteReminder() {
        val reminder = uiState.reminderDetails.toReminder()
        schedulerRepository.cancelNotificationAlarms(
            reminder.id,
            0,
            uiState.previousNotificationCount
        )
        remindersRepository.deleteReminder(reminder)
    }
}
