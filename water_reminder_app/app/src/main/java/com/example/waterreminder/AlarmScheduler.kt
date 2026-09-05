package com.example.waterreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * מתזמן אזעקה מדויקת יחידה (לא חוזרת) לתזכורת הבאה. כל פעם שהאזעקה
 * מופעלת, ה-AlarmReceiver מתזמן מיד את הבאה בתור - כך המרווח מדויק
 * גם על אנדרואיד מודרני (שלא מאפשר אזעקות חוזרות מדויקות אמיתיות).
 */
object AlarmScheduler {

    private const val REQUEST_CODE = 5001

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }
        return true
    }

    /** מתזמן את התזכורת הבאה בעוד `minutesFromNow` דקות. */
    fun scheduleNext(context: Context, minutesFromNow: Int) {
        if (!PrefsHelper.isEnabled(context)) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutesFromNow)
        }.timeInMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // אין הרשאה לאזעקות מדויקות - נופלים חזרה על אזעקה לא-מדויקת
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
    }
}
