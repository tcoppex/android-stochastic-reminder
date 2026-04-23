package cc.polysfaer.stochapop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.ReminderDao
import cc.polysfaer.stochapop.data.reminder.ReminderDayConverter
import cc.polysfaer.stochapop.data.reminder.ReminderTimeConverter

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
@TypeConverters(ReminderTimeConverter::class, ReminderDayConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    // .createFromAsset("welcome_tutorial.db")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
