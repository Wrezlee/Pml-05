package putra.yanuar.pml05

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import android.Manifest
import putra.yanuar.pml05.databinding.ActivityMessagingBinding


class MessagingActivity : AppCompatActivity() {
    lateinit var b: ActivityMessagingBinding
    // topic untuk berlangganan notifikasi
    val TOPIC = "INFO_DISKON_DAN_PROMO"
    //TAG untuk membedakan pesan Logcat dari aktivitas ini
    val TAG = "MessagingActivity"
    //menyimpan data tambahan
    var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(b.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(b.main.id)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        //notification channel untuk menampilkan notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //mengambil notificationManager dari sistem Android
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            //membuat Notification Channel
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    TOPIC, TOPIC,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        //izin menampilkan notifikasi
        askNotificationPermission()

        //berlangganan ke topic tertentu
        Firebase.messaging.subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var msg = "Berhasil subscribe"
                if (!task.isSuccessful)
                    msg = "Tidak berhasil subscribe"
                Log.d(TAG, msg)
                Toast.makeText(
                    this@MessagingActivity, msg, Toast.LENGTH_SHORT
                ).show()
            }

        //meminta token sebagai identifier device yang terhubung ke FCM
        Firebase.messaging.token.addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            val token = it.result
            Log.d(TOPIC, "Token = $token")
            b.edToken.setText("$token")
        }
        //mengecek data tambahan yang dikirim (jika ada)
        try {
            bundle = intent.extras!!
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }
        if (bundle != null) {
            //ambil semua nama key dari data yang diterima
            val keys = bundle!!.keySet()
            for (key in keys)
                Log.d(
                    TAG,
                    "$key : ${bundle!!.getString(key)}"
                )
            b.edPromoId.setText(bundle!!.getString("promoId"))
            b.edPromo.setText(bundle!!.getString("promo"))
            b.edPromoUntil.setText(bundle!!.getString("promoUntil"))
            try {
                b.edTitle.setText(bundle!!.getString("title"))
                b.edBody.setText(bundle!!.getString("body"))
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
            }
        }
    }

    //izin menampilkan notifikasi
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    this, "Izin notifikasi diberikan",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, "FCM tidak dapat berjalan tanpa izin notifikasi",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}