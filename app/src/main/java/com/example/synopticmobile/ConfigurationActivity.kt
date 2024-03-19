package com.example.synopticmobile

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
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
        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setViewVisibility(R.id.warningText, View.GONE)
        val buttonValletta: Button = findViewById(R.id.buttonValletta)
        val buttonParis: Button = findViewById(R.id.buttonParis)
        val buttonRome: Button = findViewById(R.id.buttonRome)

        buttonValletta.setOnClickListener { findAndSet("Valletta") }
        buttonParis.setOnClickListener { findAndSet("Paris") }
        buttonRome.setOnClickListener { findAndSet("Rome") }
    }
    private fun updateWidgetWeather(city: String, weatherData: JSONObject) {
        val temperature = weatherData.optInt("temp")
        val condition = weatherData.optString("condition")
        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.weatherTextView, "Location: $city, Temperature: $temperature°C, Condition: $condition")
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetComponentName = ComponentName(this, MultiWidgetProvider::class.java)
        appWidgetManager.updateAppWidget(widgetComponentName, views)
    }
    private fun findAndSet(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val weatherData = findWeatherData(city)
                updateWidgetWeather(city, weatherData)
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



}
