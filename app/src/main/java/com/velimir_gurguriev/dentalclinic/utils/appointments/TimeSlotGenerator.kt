package com.velimir_gurguriev.dentalclinic.utils.appointments

import com.velimir_gurguriev.dentalclinic.models.appointments.TimeSlotItem
import java.util.Calendar

object TimeSlotGenerator {

    private const val SLOT_DURATION_MINUTES = 30

    fun generate(
        startHour: Int,
        endHour: Int
    ): List<TimeSlotItem> {

        val timeSlots = mutableListOf<TimeSlotItem>()

        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (true) {
            val startSlotHour =
                currentTime.get(Calendar.HOUR_OF_DAY)

            val startSlotMinute =
                currentTime.get(Calendar.MINUTE)

            currentTime.add(
                Calendar.MINUTE,
                SLOT_DURATION_MINUTES
            )

            val endSlotHour =
                currentTime.get(Calendar.HOUR_OF_DAY)

            val endSlotMinute =
                currentTime.get(Calendar.MINUTE)

            if (
                endSlotHour > endHour ||
                (
                        endSlotHour == endHour &&
                                endSlotMinute > 0
                        )
            ) {
                break
            }

            timeSlots.add(
                TimeSlotItem(
                    startHour = startSlotHour,
                    startMinute = startSlotMinute,
                    endHour = endSlotHour,
                    endMinute = endSlotMinute
                )
            )
        }

        return timeSlots
    }
}