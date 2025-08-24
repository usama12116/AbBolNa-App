package com.example.chatroomapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class CategoryActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var topicRV: RecyclerView
    private lateinit var topicAdapter: TopicAdapter

    private var notificationService: NotificationService? = null
    private var isServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        mAuth = FirebaseAuth.getInstance()

        val topics = arrayOf("Main","Anime", "Movies", "Gaming", "Books", "Music")

        val intent = Intent(this@CategoryActivity, com.example.chatroomapp.NotificationService::class.java)
        intent.putExtra("topics", topics)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        topicRV = findViewById(R.id.topicRV)
        topicAdapter = TopicAdapter(this, topics.toList())

        val layoutManager = LinearLayoutManager(this)
        topicRV.layoutManager = layoutManager

        topicRV.adapter = topicAdapter

        topicAdapter.setOnItemClickListener(object : TopicAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Start the intent with the selected topic name
                val selectedTopic = topics[position].lowercase()
                val intent = Intent(this@CategoryActivity, MainActivity::class.java)
                intent.putExtra("selectedTopic", selectedTopic)
                startActivity(intent)
            }
        })
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceBound = true
            val binder = service as NotificationService.MyBinder
            notificationService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle service disconnection
            isServiceBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        val topics = arrayOf("Main","Anime", "Movies", "Gaming", "Books", "Music")

        val intent = Intent(this@CategoryActivity, com.example.chatroomapp.NotificationService::class.java)
        intent.putExtra("topics", topics)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.optionsmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logoutButton){
            mAuth.signOut()
            val intent = Intent(this@CategoryActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        if(item.itemId == R.id.profileButton){
            val intent = Intent(this@CategoryActivity, ProfileEditorActivity::class.java)
            startActivity(intent)
        }
        if(item.itemId == R.id.profileSearchButton){
            val intent = Intent(this@CategoryActivity, ProfileSearcherActivity::class.java)
            startActivity(intent)
        }
        return true
    }
}