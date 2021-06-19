package com.sgpublic.scit.tool.api.module

import com.sgpublic.scit.tool.api.result.SuccessResult
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
class DayModule {
    companion object {
        private const val SEMESTER = 2
        private const val SCHOOL_YEAR = "2020-2021"
        private const val EVALUATION = false
        private val DATE_START: Calendar get() {
            dateStart?.let {
                return it
            }
            val date = Calendar.getInstance()
            date.set(2021, 2, 28, 0, 0, 0)
            return date
        }

        private var dateStart: Calendar? = null

        fun getDay(): Map<String, Any> {
            val timeNow = Calendar.getInstance().timeInMillis
            val timeBetween = (timeNow - DATE_START.timeInMillis) / 86400000
            val sdf = SimpleDateFormat.getDateInstance()

            return SuccessResult(
                "day_count" to timeBetween,
                "date" to sdf.format(DATE_START),
                "semester" to SEMESTER,
                "school_year" to SCHOOL_YEAR,
                "evaluation" to EVALUATION
            )
        }
    }
}