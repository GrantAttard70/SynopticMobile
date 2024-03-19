package com.example.synopticmobile
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {




    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val title = it.title ?: "Notification"
            val body = it.body ?: ""
            displayNotification(title, body)

            // Update widget after displaying notification
            updateWarningVisibility()
        }
    }

    private fun displayNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "NotificationChannelID"
            val channel = NotificationChannel(channelId, "NotificationChannelName", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "NotificationChannelID")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.color.transparent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0, notificationBuilder.build())

    }
    private fun updateWarningVisibility() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(this, MultiWidgetProvider::class.java))
        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setViewVisibility(R.id.warningText, View.VISIBLE)


        appWidgetManager.updateAppWidget(widgetIds, views)
    }
}
