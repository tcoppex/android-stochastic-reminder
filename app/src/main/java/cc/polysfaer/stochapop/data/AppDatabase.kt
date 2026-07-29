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
import cc.polysfaer.stochapop.data.reminder.ReminderScheduleType
import cc.polysfaer.stochapop.data.reminder.ReminderScheduleTypeConverter
import cc.polysfaer.stochapop.data.reminder.ReminderSettings
import cc.polysfaer.stochapop.data.reminder.ReminderTimeConverter
import cc.polysfaer.stochapop.data.reminder.ReminderUriConverter

@Database(
    entities = [Reminder::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    ReminderScheduleTypeConverter::class,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Note: As we support Android API < 31 we must recreate the whole table to drop a column.

                db.execSQL(sql =
                    """
                    CREATE TABLE IF NOT EXISTS `reminders_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `hasSound` INTEGER NOT NULL,
                        `hasVibration` INTEGER NOT NULL,
                        `soundUri` TEXT,
                        `scheduleType` INTEGER NOT NULL,
                        `notificationCount` INTEGER NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER NOT NULL,
                        `selectedDays` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Adapt previous column 'useRandomRange' to fit 'scheduleType' and transfer the rest.
                db.execSQL(sql =
                    """
                    INSERT INTO `reminders_new` (
                        `id`, `title`, `message`, `enabled`, `hasSound`, `hasVibration`,
                        `soundUri`, `scheduleType`, `notificationCount`, `startTime`, `endTime`, `selectedDays`
                    )
                    SELECT 
                        `id`, `title`, `message`, `enabled`, `hasSound`, `hasVibration`, `soundUri`, 
                        CASE WHEN `useRandomRange` = 1 
                            THEN ${ReminderScheduleType.RANDOM.ordinal} 
                            ELSE ${ReminderScheduleType.FIXED.ordinal} 
                        END,
                        `notificationCount`, `startTime`, `endTime`, `selectedDays`
                    FROM `reminders`
                    """.trimIndent()
                )

                // Swap the old table with the new one.
                db.execSQL("DROP TABLE `reminders`")
                db.execSQL("ALTER TABLE `reminders_new` RENAME TO `reminders`")
            }
        }

        private val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
        )

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
