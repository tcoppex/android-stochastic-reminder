package cc.polysfaer.stochapop.data.reminder

enum class ReminderScheduleType(val useRange: Boolean) {
    FIXED(false),
    RANGED(true),
    RANDOM(true),
}
