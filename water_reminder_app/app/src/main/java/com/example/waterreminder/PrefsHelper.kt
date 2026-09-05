package com.example.waterreminder

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrefsHelper {

    private const val PREFS_NAME = "water_reminder_prefs"

    private const val KEY_ENABLED = "enabled"
    private const val KEY_INTERVAL_MINUTES = "interval_minutes"
    private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
    private const val KEY_ACTIVE_START_HOUR = "active_start_hour"
    private const val KEY_ACTIVE_END_HOUR = "active_end_hour"
    private const val KEY_CUPS_TODAY = "cups_today"
    private const val KEY_CUPS_DATE = "cups_date"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun todayString(): String = dateFormat.format(Date())

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)
    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun getIntervalMinutes(context: Context) = prefs(context).getInt(KEY_INTERVAL_MINUTES, 60)
    fun setIntervalMinutes(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_INTERVAL_MINUTES, value).apply()
    }

    fun getSnoozeMinutes(context: Context) = prefs(context).getInt(KEY_SNOOZE_MINUTES, 10)
    fun setSnoozeMinutes(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_SNOOZE_MINUTES, value).apply()
    }

    /** שעות פעילות התזכורות (24 שעות). ברירת מחדל: 08:00 - 22:00. */
    fun getActiveStartHour(context: Context) = prefs(context).getInt(KEY_ACTIVE_START_HOUR, 8)
    fun getActiveEndHour(context: Context) = prefs(context).getInt(KEY_ACTIVE_END_HOUR, 22)
    fun setActiveHours(context: Context, startHour: Int, endHour: Int) {
        prefs(context).edit()
            .putInt(KEY_ACTIVE_START_HOUR, startHour)
            .putInt(KEY_ACTIVE_END_HOUR, endHour)
            .apply()
    }

    fun isWithinActiveHours(context: Context, hourOfDay: Int): Boolean {
        val start = getActiveStartHour(context)
        val end = getActiveEndHour(context)
        return if (start <= end) hourOfDay in start until end
        else hourOfDay >= start || hourOfDay < end // טווח שחוצה חצות
    }

    private fun resetIfNewDay(context: Context) {
        val p = prefs(context)
        if (p.getString(KEY_CUPS_DATE, null) != todayString()) {
            p.edit()
                .putString(KEY_CUPS_DATE, todayString())
                .putInt(KEY_CUPS_TODAY, 0)
                .apply()
        }
    }

    fun getCupsToday(context: Context): Int {
        resetIfNewDay(context)
        return prefs(context).getInt(KEY_CUPS_TODAY, 0)
    }

    fun addCup(context: Context) {
        resetIfNewDay(context)
        val current = getCupsToday(context)
        prefs(context).edit().putInt(KEY_CUPS_TODAY, current + 1).apply()
    }
}
