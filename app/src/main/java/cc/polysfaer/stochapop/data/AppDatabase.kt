package cc.polysfaer.stochapop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.data.reminder.ReminderDao
import cc.polysfaer.stochapop.data.reminder.ReminderDayConverter
import cc.polysfaer.stochapop.data.reminder.ReminderSettings
import cc.polysfaer.stochapop.data.reminder.ReminderTimeConverter
import cc.polysfaer.stochapop.data.reminder.ReminderUriConverter

@Database(
    entities = [Reminder::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    ReminderTimeConverter::class,
    ReminderDayConverter::class,
    ReminderUriConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val defaultUriString = ReminderSettings.DEFAULT_NOTIFICATION_URI.toString() //
                db.execSQL(
                    "ALTER TABLE reminders ADD COLUMN soundUri TEXT DEFAULT '$defaultUriString'"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
