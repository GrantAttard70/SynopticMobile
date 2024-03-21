package com.example.synopticmobile

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class ConfigurationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_activity)

        updateWidgets("Valletta")

        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setViewVisibility(R.id.warningText, View.GONE)
        val buttonValletta: Button = findViewById(R.id.buttonValletta)
        val buttonParis: Button = findViewById(R.id.buttonParis)
        val buttonRome: Button = findViewById(R.id.buttonRome)

        buttonValletta.setOnClickListener { findAndSet("Valletta") }
        buttonParis.setOnClickListener { findAndSet("Paris") }
        buttonRome.setOnClickListener { findAndSet("Rome") }

        scheduleBatteryWidgetUpdates()
    }

    private fun updateWidgets(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val weatherData = findWeatherData(city)
                updateWidget(city, weatherData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateWidget(city: String, weatherData: JSONObject) {
        val temperature = weatherData.optInt("temp")
        val condition = weatherData.optString("condition")
        val batteryLevel = getBatteryLevel()
        val iconResId = when {
            batteryLevel >= 75 -> R.drawable.full
            batteryLevel >= 45 -> R.drawable.orange
            else -> R.drawable.red
        }
        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setTextViewText(
            R.id.weatherTextView,
            "Location: $city, Temperature: $temperature°C, Condition: $condition"
        )
        views.setImageViewResource(R.id.batteryIcon, iconResId)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetComponentName = ComponentName(this, ConfigurationActivity::class.java)
        appWidgetManager.updateAppWidget(widgetComponentName, views)
    }

    private fun scheduleBatteryWidgetUpdates() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, ConfigurationActivity::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        //  every 2 seconds for testing purposes
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            2000,
            alarmIntent
        )
    }

    private fun findAndSet(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val weatherData = findWeatherData(city)
                runOnUiThread {
                    updateWidget(city, weatherData)
                }
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findWeatherData(city: String): JSONObject {
        val apiUrl = "https://api.jamesdecelis.com/api/v1/weather/$city"
        val jsonString = URL(apiUrl).readText()
        return JSONObject(jsonString)
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
