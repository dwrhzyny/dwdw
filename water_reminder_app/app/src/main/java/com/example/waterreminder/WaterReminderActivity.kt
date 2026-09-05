package com.example.waterreminder

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * מסך מלא בסגנון "אזעקה" שנפתח מעל כל דבר אחר (גם מעל מסך נעילה).
 * חייבים ללחוץ על אחד הכפתורים כדי לסגור אותו - זה מה שעוצר את הצליל/רטט החוזרים.
 */
class WaterReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreenAndTurnOnScreen()
        setContentView(R.layout.activity_water_reminder)

        val cupsText = findViewById<TextView>(R.id.text_cups_today)
        cupsText.text = getString(R.string.cups_today_format, PrefsHelper.getCupsToday(this))

        findViewById<Button>(R.id.button_drank_water).setOnClickListener {
            PrefsHelper.addCup(this)
            NotificationHelper.cancel(this)
            AlarmScheduler.scheduleNext(this, PrefsHelper.getIntervalMinutes(this))
            finish()
        }

        findViewById<Button>(R.id.button_snooze).setOnClickListener {
            NotificationHelper.cancel(this)
            AlarmScheduler.scheduleNext(this, PrefsHelper.getSnoozeMinutes(this))
            finish()
        }
    }

    private fun showOverLockScreenAndTurnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    // מונעים סגירה בלחיצת "חזרה" בלי לבחור אחת מהאפשרויות -
    // זה שקול ל"דחייה" כדי שלא יישאר תקוע עם ההתראה פתוחה ברקע
    override fun onBackPressed() {
        NotificationHelper.cancel(this)
        AlarmScheduler.scheduleNext(this, PrefsHelper.getSnoozeMinutes(this))
        finish()
    }
}
