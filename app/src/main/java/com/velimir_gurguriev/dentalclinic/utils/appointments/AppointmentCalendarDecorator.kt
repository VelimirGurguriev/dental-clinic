package com.velimir_gurguriev.dentalclinic.utils.appointments

import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.velimir_gurguriev.dentalclinic.R
import java.util.Calendar

object AppointmentCalendarDecorator {

    fun applyWeekendColors(
        calendarView: CalendarView
    ) {
        val weekendDays =
            mutableListOf<CalendarDay>()

        val startDate =
            AppointmentDateUtils.getToday().apply {
                add(Calendar.YEAR, -1)
            }

        val endDate =
            AppointmentDateUtils.getToday().apply {
                add(Calendar.YEAR, 5)
            }

        val currentDate =
            startDate.clone() as Calendar

        while (!currentDate.after(endDate)) {

            if (
                AppointmentDateUtils.isWeekend(
                    currentDate
                )
            ) {
                weekendDays.add(
                    CalendarDay(
                        currentDate.clone() as Calendar
                    ).apply {
                        labelColor =
                            R.color.calendar_weekend
                    }
                )
            }

            currentDate.add(
                Calendar.DAY_OF_MONTH,
                1
            )
        }

        calendarView.setCalendarDays(
            weekendDays
        )
    }
}