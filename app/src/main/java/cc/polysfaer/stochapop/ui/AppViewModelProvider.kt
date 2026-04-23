package cc.polysfaer.stochapop.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cc.polysfaer.stochapop.MainApplication
import cc.polysfaer.stochapop.ui.screens.home.HomeViewModel
import cc.polysfaer.stochapop.ui.screens.reminder.ReminderEditViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = mainApplication()
            val appContainer = app.container

            HomeViewModel(
                app,
                appContainer.remindersRepository,
                appContainer.schedulerRepository,
                appContainer.prefs
            )
        }
        initializer {
            val app = mainApplication()
            val appContainer = app.container

            ReminderEditViewModel(
                this.createSavedStateHandle(),
                app,
                appContainer.remindersRepository,
                appContainer.schedulerRepository
            )
        }
    }
}

fun CreationExtras.mainApplication(): MainApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication)
