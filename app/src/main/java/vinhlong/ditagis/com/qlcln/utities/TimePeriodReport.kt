package vinhlong.ditagis.com.qlcln.utities

import android.content.Context
import android.text.format.DateFormat
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.ThongKeAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */

class TimePeriodReport(private val mContext: Context) {
    private lateinit var  calendar: Calendar
    private lateinit var today: Date
    private var items: MutableList<ThongKeAdapter.Item>? = null

    private val firstDayofMonth: Date
        get() {
            resetToday()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            return calendar.time
        }

    private val lastDayofMonth: Date
        get() {
            getActualMaximumToday()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLastMonth: Date
        get() {
            resetToday()
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            return calendar.time
        }

    private val lastDayofLastMonth: Date
        get() {
            getActualMaximumToday()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLast3Months: Date
        get() {
            resetToday()
            calendar.add(Calendar.MONTH, -2)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            return calendar.time
        }

    private val lastDayofLast3Months: Date
        get() {
            getActualMaximumToday()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLast6Months: Date
        get() {
            resetToday()
            calendar.add(Calendar.MONTH, -5)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            return calendar.time
        }

    private val lastDayofLast6Months: Date
        get() {
            getActualMaximumToday()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofYear: Date
        get() {
            resetToday()
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            return calendar.time
        }

    private val lastDayofYear: Date
        get() {
            getActualMaximumToday()
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.set(Calendar.MONTH, 11)
            return calendar.time
        }

    private val firstDayoflLastYear: Date
        get() {
            resetToday()
            calendar.add(Calendar.YEAR, -1)
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            return calendar.time
        }

    private val lastDayofLastYear: Date
        get() {
            getActualMaximumToday()
            calendar.add(Calendar.YEAR, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.set(Calendar.MONTH, 11)
            return calendar.time
        }

    init {
        today = Date()
        calendar = Calendar.getInstance()
        items = arrayListOf()
        items!!.add(ThongKeAdapter.Item(1, "Tất cả", null!!, null!!, null!!))
        items!!.add(ThongKeAdapter.Item(2, "Tháng này", formatTimeToGMT(firstDayofMonth), formatTimeToGMT(lastDayofMonth), dayToFirstDayString(firstDayofMonth, lastDayofMonth)))
        items!!.add(ThongKeAdapter.Item(3, "Tháng trước", formatTimeToGMT(firstDayofLastMonth), formatTimeToGMT(lastDayofLastMonth), dayToFirstDayString(firstDayofLastMonth, lastDayofLastMonth)))
        items!!.add(ThongKeAdapter.Item(4, "3 tháng gần nhất", formatTimeToGMT(firstDayofLast3Months), formatTimeToGMT(lastDayofLast3Months), dayToFirstDayString(firstDayofLast3Months, lastDayofLast3Months)))
        items!!.add(ThongKeAdapter.Item(5, "6 tháng gần nhất", formatTimeToGMT(firstDayofLast6Months), formatTimeToGMT(lastDayofLast6Months), dayToFirstDayString(firstDayofLast6Months, lastDayofLast6Months)))
        items!!.add(ThongKeAdapter.Item(6, "Năm nay", formatTimeToGMT(firstDayofYear), formatTimeToGMT(lastDayofYear), dayToFirstDayString(firstDayofYear, lastDayofYear)))
        items!!.add(ThongKeAdapter.Item(7, "Năm trước", formatTimeToGMT(firstDayoflLastYear), formatTimeToGMT(lastDayofLastYear), dayToFirstDayString(firstDayoflLastYear, lastDayofLastYear)))
        items!!.add(ThongKeAdapter.Item(8, "Tùy chỉnh", null!!, null!!, "-- - --"))
    }

    fun getItems(): List<ThongKeAdapter.Item>? {
        return items
    }

    fun setItems(items: MutableList<ThongKeAdapter.Item>) {
        this.items = items
    }

    private fun formatTimeToGMT(date: Date): String {
        val dateFormatGmt = SimpleDateFormat(mContext.getString(R.string.format_day_yearfirst))
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(date)
    }

    private fun dayToFirstDayString(firstDate: Date, lastDate: Date): String {
        return DateFormat.format(mContext.getString(R.string.format_time_day_month_year), firstDate) as String + " - " + DateFormat.format(mContext.getString(R.string.format_time_day_month_year), lastDate) as String
    }

    private fun resetToday() {
        calendar.time = today
        calendar.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
    }

    private fun getActualMaximumToday() {
        calendar.time = today
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
    }
}
