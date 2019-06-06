
package com.uuuk.smart_home_aa

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


/**
 * FirebaseNotification
 * Created by Sean Lin on 2017/8/21 下午11:30.
 */
class firebase_mes : FirebaseMessagingService() {

    lateinit var broadcast:LocalBroadcastManager
    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        broadcast = LocalBroadcastManager.getInstance(this)
        Log.e("firebase message", message?.notification?.body)
        var contentText = ""
        if (message?.notification?.body != null) contentText = message.notification!!.body.toString()

        sendNotification(contentText)
    }

    private fun sendNotification(message:String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("1", "超標通知", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
            builder = Notification.Builder(this, "1")
        }
        else{
            builder=Notification.Builder(this)
        }
        builder.setSmallIcon(R.drawable.home)
            .setContentTitle("超標通知")
            .setContentText(message)
            .setAutoCancel(true)
        manager.notify(0,builder.build())
    }
}
