package com.example.synopticmobile

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MultiWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val city = prefs.getString("city", "valletta") ?: "valletta " //default value :)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val weatherData = findWeatherData(city)
                val temperature = weatherData?.optInt("temp") ?: -1
                val condition = weatherData?.optString("condition") ?: "Unknown"


                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.weatherTextView, "Temperature: $temperature°C, Condition: $condition")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findWeatherData(city: String): JSONObject? {
        val apiUrl = "https://api.jamesdecelis.com/api/v1/weather/$city"
        val url = URL(apiUrl)
        val connection = url.openConnection() as? HttpsURLConnection
        return try {
            connection?.connectTimeout = 5000
            connection?.readTimeout = 5000
            val inputStream = connection?.inputStream
            val jsonString = inputStream?.bufferedReader().use { it?.readText() } ?: ""
            JSONObject(jsonString)
        } finally {
            connection?.disconnect()
        }
    }

}
