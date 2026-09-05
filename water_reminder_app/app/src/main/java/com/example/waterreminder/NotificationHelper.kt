package com.example.waterreminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * בונה התראה "בלתי ניתנת להתעלמות":
 * - Notification.FLAG_INSISTENT: הצליל/רטט חוזרים על עצמם שוב ושוב עד שההתראה מבוטלת
 *   (בדיוק כמו באפליקציית שעון מעורר), במקום לצפצף פעם אחת ולהיעלם.
 * - setOngoing(true): לא ניתן להסיר בהחלקה - חייבים ללחוץ על כפתור בתוך המסך.
 * - fullScreenIntent: פותח מסך מלא (WaterReminderActivity) גם אם המסך נעול/כבוי,
 *   בדיוק כמו שיחה נכנסת או אזעקה.
 */
object NotificationHelper {

    const val CHANNEL_ID = "water_reminder_channel"
    const val NOTIFICATION_ID = 3001

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_description)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setSound(
                        alarmSound,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showWaterReminder(context: Context) {
        ensureChannel(context)

        val fullScreenIntent = Intent(context, WaterReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // לא ניתן להחליק ולהיפטר ממנה
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)

        val notification = builder.build()
        // הדגל הזה גורם לצליל/לרטט לחזור על עצמם עד שההתראה תבוטל
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // המשתמש לא אישר הרשאת התראות
        }

        // גם אם המכשיר פתוח וההרשאה למסך-מלא לא זמינה, ננסה בכל זאת לפתוח את המסך ישירות
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // לא קריטי - ההתראה עצמה עדיין תוצג
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
