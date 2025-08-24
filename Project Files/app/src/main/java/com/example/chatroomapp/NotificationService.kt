package com.example.chatroomapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationService : Service() {

    private val mBinder: IBinder = MyBinder()
    private lateinit var mDbRef : DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var backgroundThread: Thread

    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
    }

    override fun onBind(intent: Intent?): IBinder {
        //Retrieve the category from the intent extras
        val category = intent?.getStringArrayExtra("topics")

        if (!category.isNullOrEmpty()) {
            // Loop over the topics and set up listeners for each category
            for (topic in category) {
                if (topic.isNotBlank()) {
                    createNotificationChannel(topic.lowercase())

                    backgroundThread = Thread {
                        mDbRef.child("chats").child(topic.lowercase()).child("messages")
                            .addValueEventListener(createValueEventListener(topic.lowercase()))
                    }
                    backgroundThread.start()
                }
            }
        }
        return mBinder
    }

    override fun onDestroy() {
        // Interrupt and join the background thread
        backgroundThread.interrupt()
        backgroundThread.join()
        super.onDestroy()
    }

    inner class MyBinder : Binder() {
        fun getService(): NotificationService {
            return this@NotificationService
        }
    }

    private fun createValueEventListener(category: String): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = ArrayList<com.example.chatroomapp.Message>()

                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(com.example.chatroomapp.Message::class.java)
                    message?.let {
                        newMessages.add(it)
                    }
                }
                val latestMessage = newMessages.lastOrNull()

                if (latestMessage != null && latestMessage.senderID != mAuth.currentUser?.uid) {
                    latestMessage.name?.let { senderName ->
                        showNotification(category, senderName, latestMessage.message ?: "")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        }
    }

    private fun createNotificationChannel(category: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "chatroom_channel_$category"
            val channelName = "Chatroom Channel - $category"

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Register the channel with the system.
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(category: String, senderName: String, message: String) {
        val channelId = "chatroom_channel_$category"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("selectedTopic", category.lowercase())
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("New Message in $category")
            .setContentText("From: $senderName, Message: $message")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@NotificationService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Handle the case where notification permission is not granted
                return
            }
            notify(NOTIFICATION_ID, notification.build())
        }
    }
}