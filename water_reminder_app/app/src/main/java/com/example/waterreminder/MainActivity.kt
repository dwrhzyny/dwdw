package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermissionIfNeeded()
        loadSettingsIntoUi()

        findViewById<Button>(R.id.button_save).setOnClickListener {
            saveSettingsFromUi()
        }

        findViewById<Button>(R.id.button_grant_exact_alarms).setOnClickListener {
            openExactAlarmSettings()
        }

        findViewById<Button>(R.id.button_test_now).setOnClickListener {
            NotificationHelper.showWaterReminder(this)
        }

        findViewById<Button>(R.id.button_add_cup_manually).setOnClickListener {
            PrefsHelper.addCup(this)
            updateCupsDisplay()
        }

        updateCupsDisplay()
    }

    override fun onResume() {
        super.onResume()
        updateCupsDisplay()
        updateExactAlarmWarning()
    }

    private fun loadSettingsIntoUi() {
        findViewById<Switch>(R.id.switch_enabled).isChecked = PrefsHelper.isEnabled(this)
        findViewById<EditText>(R.id.edit_interval).setText(PrefsHelper.getIntervalMinutes(this).toString())
        findViewById<EditText>(R.id.edit_snooze).setText(PrefsHelper.getSnoozeMinutes(this).toString())
        findViewById<EditText>(R.id.edit_start_hour).setText(PrefsHelper.getActiveStartHour(this).toString())
        findViewById<EditText>(R.id.edit_end_hour).setText(PrefsHelper.getActiveEndHour(this).toString())
    }

    private fun saveSettingsFromUi() {
        val enabled = findViewById<Switch>(R.id.switch_enabled).isChecked
        val interval = findViewById<EditText>(R.id.edit_interval).text.toString().toIntOrNull()
        val snooze = findViewById<EditText>(R.id.edit_snooze).text.toString().toIntOrNull()
        val startHour = findViewById<EditText>(R.id.edit_start_hour).text.toString().toIntOrNull()
        val endHour = findViewById<EditText>(R.id.edit_end_hour).text.toString().toIntOrNull()

        if (interval == null || interval <= 0 || snooze == null || snooze <= 0 ||
            startHour == null || startHour !in 0..23 || endHour == null || endHour !in 0..23
        ) {
            Toast.makeText(this, getString(R.string.toast_invalid_settings), Toast.LENGTH_SHORT).show()
            return
        }

        PrefsHelper.setEnabled(this, enabled)
        PrefsHelper.setIntervalMinutes(this, interval)
        PrefsHelper.setSnoozeMinutes(this, snooze)
        PrefsHelper.setActiveHours(this, startHour, endHour)

        if (enabled) {
            AlarmScheduler.scheduleNext(this, interval)
            Toast.makeText(this, getString(R.string.toast_reminders_on), Toast.LENGTH_SHORT).show()
        } else {
            AlarmScheduler.cancel(this)
            Toast.makeText(this, getString(R.string.toast_reminders_off), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCupsDisplay() {
        findViewById<TextView>(R.id.text_cups_today)
            .text = getString(R.string.cups_today_format, PrefsHelper.getCupsToday(this))
    }

    private fun updateExactAlarmWarning() {
        val warning = findViewById<TextView>(R.id.text_exact_alarm_warning)
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !AlarmScheduler.canScheduleExactAlarms(this)
        warning.visibility = if (needsPermission) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.toast_no_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001
                )
            }
        }
    }
}
