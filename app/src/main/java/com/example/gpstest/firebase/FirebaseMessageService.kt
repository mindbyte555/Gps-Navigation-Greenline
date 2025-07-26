package com.example.gpstest.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gpstest.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class FirebaseMessageService : FirebaseMessagingService() {
    private lateinit var pendingIntent: PendingIntent
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: ""
            val message = notification.body ?: ""
            val uri = notification.imageUrl ?: ""
            Log.e(
                "FCM",
                "onMessageReceived notification: title: $title , message: $message and url: $uri"
            )
            CoroutineScope(Dispatchers.IO).launch {
                showNotification(title, message, uri.toString())
            }
        }
    }

    private fun showNotification(title: String, message: String, imageUrl: String?) {
        val notificationId = 1001
        Log.e("FCM", "showNotification: else")

        val dismissIntent = Intent(this, NotificationDismissReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
        }
        pendingIntent = PendingIntent.getBroadcast(
            this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fcm_channel_id", "Notification Channel", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val bigPicture: Bitmap? = imageUrl?.let { fetchBitmapFromUrl(it) }

        val notificationBuilder =
            NotificationCompat.Builder(this, "fcm_channel_id").setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.icon3) // Replace with your app icon
                .setAutoCancel(true).setContentIntent(pendingIntent).setTimeoutAfter(200000)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        // If an image is fetched, add BigPictureStyle
        bigPicture?.let {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(it)
            )
        }
        // Build and show the notification
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationId, notification)
    }

    private fun fetchBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}