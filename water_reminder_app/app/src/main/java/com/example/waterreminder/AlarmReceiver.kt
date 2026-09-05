package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!PrefsHelper.isEnabled(context)) return

        val hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (PrefsHelper.isWithinActiveHours(context, hourNow)) {
            NotificationHelper.showWaterReminder(context)
            AlarmScheduler.scheduleNext(context, PrefsHelper.getIntervalMinutes(context))
        } else {
            // מחוץ לשעות הפעילות - נבדוק שוב בעוד 30 דקות בלי להציג תזכורת עכשיו
            AlarmScheduler.scheduleNext(context, 30)
        }
    }
}
