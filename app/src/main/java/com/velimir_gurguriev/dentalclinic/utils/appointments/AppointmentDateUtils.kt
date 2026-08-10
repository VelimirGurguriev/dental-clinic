package com.velimir_gurguriev.dentalclinic.utils.appointments

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppointmentDateUtils {

    private val bulgarianLocale =
        Locale("bg", "BG")

    fun clearTime(
        calendar: Calendar
    ) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun getToday(): Calendar {
        return Calendar.getInstance().apply {
            clearTime(this)
        }
    }

    fun isPastDate(
        calendar: Calendar
    ): Boolean {
        val date =
            calendar.clone() as Calendar

        clearTime(date)

        return date.before(getToday())
    }

    fun isWeekend(
        calendar: Calendar
    ): Boolean {
        val dayOfWeek =
            calendar.get(Calendar.DAY_OF_WEEK)

        return dayOfWeek == Calendar.SATURDAY ||
                dayOfWeek == Calendar.SUNDAY
    }

    fun createDateTime(
        date: Long,
        hour: Int,
        minute: Int
    ): Long {
        return Calendar.getInstance().apply {
            timeInMillis = date

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatDate(
        date: Long
    ): String {
        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy",
            bulgarianLocale
        )

        return dateFormat.format(
            Date(date)
        )
    }
}