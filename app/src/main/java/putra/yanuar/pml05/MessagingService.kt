package putra.yanuar.pml05

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    val TAG = "FCMService"
    val TOPIC = "INFO_DISKON_DAN_PROMO"
    val RC_INTENT = 100
    var promoId = ""
    var promo = ""
    var promoUntil = ""
    var msgTitle = ""
    var msgBody = ""

    //ketika FCM memberikan token baru
    override fun onNewToken(token: String) {
        Log.d(TAG, "token baru = $token")
    }

    //ketika aplikasi menerima pesan dari FCM
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("msg", "msg = ${message.data}")
        val intent = Intent(this, MessagingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        msgTitle = message.notification!!.title!!
        msgBody = message.notification!!.body!!
        //jika notif berisi data
        if (message.data.isNotEmpty()) {
            Log.d("msg", "msg = ${message}")
            promoId = message.data["promoId"]!!
            promo = message.data["promo"]!!
            promoUntil = message.data["promoUntil"]!!
            //mengirim data MessagingActivity
            intent.putExtra("promoId", promoId)
            intent.putExtra("promo", promo)
            intent.putExtra("promoUntil", promoUntil)
            intent.putExtra("title", msgTitle)
            intent.putExtra("body", msgBody)
        }
        sendNotification(msgTitle, msgBody, intent)
    }

    //menampilkan notifikasi pada perangkat
    private fun sendNotification(
        title: String, messageBody: String, intent: Intent
    ) {
        val pendingIntent = PendingIntent.getActivity(
            this, RC_INTENT, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val defaultSoundUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_RINGTONE
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        //notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TOPIC, TOPIC,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = TOPIC
            channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        //tampilan notifikasi
        val notificationBuilder =
            NotificationCompat.Builder(this, TOPIC)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources, android.R.drawable.ic_dialog_info
                    )
                )
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        notificationManager.notify(RC_INTENT, notificationBuilder.build())
    }
}