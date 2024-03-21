package com.example.synopticmobile

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MultiWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_SHOW_CHARGING_LOG = "com.example.synopticmobile.SHOW_CHARGING_LOG"

    }

    private fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    private fun updateBatteryIcon(context: Context, batteryLevel: Int, views: RemoteViews) {
        val iconResId = when {
            batteryLevel >= 75 -> R.drawable.full
            batteryLevel >= 45 -> R.drawable.orange
            else -> R.drawable.red
        }
        views.setImageViewResource(R.id.batteryIcon, iconResId)
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {
            Log.i("WidgetUpdate", "onUpdate() method called")
            updateWidget(context, appWidgetManager, appWidgetId)

        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, MultiWidgetProvider::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            2000,
            alarmIntent
        )

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val pendingIntent = createPendingIntent(context)
        views.setOnClickPendingIntent(R.id.widget, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_SHOW_CHARGING_LOG) {
            val logIntent = Intent(context, ChargingLogActivity::class.java)
            logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(logIntent)
        }
    }


    private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {


            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val views = RemoteViews(context.packageName, R.layout.widget_layout)
                    val batteryLevel = getBatteryLevel(context)
                    updateBatteryIcon(context, batteryLevel, views)

                    val pendingIntent = createPendingIntent(context)
                    views.setOnClickPendingIntent(R.id.widget, pendingIntent)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MultiWidgetProvider::class.java)
        intent.action = ACTION_SHOW_CHARGING_LOG
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    }
