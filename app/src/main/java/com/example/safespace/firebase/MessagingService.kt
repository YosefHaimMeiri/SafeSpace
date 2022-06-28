package com.example.safespace.firebase

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safespace.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {

    private lateinit var notificationChannel : NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private val id = 12345678
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token:$token")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Remote message: ${message.notification?.body}")
        createNotificationChannel()
        sendNotification(message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: Notification.Builder = Notification.Builder(this,id.toString())
            .setSmallIcon(R.drawable.btn_star)
            .setContentTitle(notification!!.title)
            .setContentText(notification.body)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel

            val id = 12345678
            val name = getString(R.string.untitled)
            val descriptionText = getString(R.string.unknownName)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            notificationChannel = NotificationChannel(id.toString(), name, importance)
            notificationChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}