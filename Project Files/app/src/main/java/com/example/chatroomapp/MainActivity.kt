package com.example.chatroomapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var chatRV: RecyclerView
    private lateinit var chatBox: EditText
    private lateinit var chatSendButton: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: ArrayList<com.example.chatroomapp.Message>

    private var category: String? = null
    var currentUser: User? = null

    private var chatService: ChatService? = null
    private var isServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        mDbRef.child("users").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val user = postSnapshot.getValue(User::class.java)
                    if(mAuth.currentUser?.uid == user?.uid){
                        currentUser = user
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        chatRV = findViewById(R.id.chatRV)
        chatBox = findViewById(R.id.chatBox)
        chatSendButton = findViewById(R.id.chatSendButton)

        category = intent.getStringExtra("selectedTopic")
        // Set the ActionBar title dynamically based on the category
        supportActionBar?.title = category?.uppercase() ?: "Chatroom App"


        val intent = Intent(this@MainActivity, com.example.chatroomapp.ChatService::class.java)
        intent.putExtra("category", category)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        chatBox.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if (b) {
                performDelayedScroll()
            }
        }

        chatBox.setOnClickListener{
            performDelayedScroll()
        }

        chatSendButton.setOnClickListener {
            val chatText = chatBox.text.toString()
            val calender = Calendar.getInstance().time
            val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT).format(calender)
            if(chatText.isNotEmpty()){
                val messageObject = com.example.chatroomapp.Message(currentUser?.name, chatText, currentUser?.uid, timeFormat)

                mDbRef.child("chats").child(category!!).child("messages").push().setValue(messageObject)
            }
            chatBox.setText("")
        }
    }

    private fun performDelayedScroll() {
        if (chatAdapter.itemCount > 0) {
            chatRV.postDelayed({
                chatRV.smoothScrollToPosition((chatRV.adapter as ChatAdapter).itemCount - 1)
            }, 500)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceBound = true
            val binder = service as ChatService.MyBinder
            chatService = binder.getService()

            // Set up your UI or perform other actions here
            initializeUI()

            // Set the callback to receive data changes
            chatService!!.setCallback(object : ChatService.Callback {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChanged(chatList: ArrayList<com.example.chatroomapp.Message>) {
                    runOnUiThread {
                        // Update your UI with the updated chatList
                        this@MainActivity.chatList.clear()
                        this@MainActivity.chatList.addAll(chatList)
                        chatAdapter.notifyDataSetChanged()
                        performDelayedScroll()
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle service disconnection
            isServiceBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        category = intent.getStringExtra("selectedTopic")
        chatList = ArrayList()
        chatList.clear()
        val intent = Intent(this, ChatService::class.java)
        intent.putExtra("category", category)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun initializeUI() {
        // Initialize your UI components and set up RecyclerView here
        chatList = chatService?.getLocallyStoredMessages() ?: ArrayList()
        Log.d("ChatList", "ChatList content: $chatList") // Add this log statement
        chatAdapter = ChatAdapter(this, chatList)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatRV.layoutManager = layoutManager
        chatRV.adapter = chatAdapter

        supportActionBar?.title = category?.uppercase() ?: "Chatroom App"

        // Trigger delayed scroll or other UI setup as needed
        performDelayedScroll()
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
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        if(item.itemId == R.id.profileButton){
            val intent = Intent(this@MainActivity, ProfileEditorActivity::class.java)
            startActivity(intent)
        }
        if(item.itemId == R.id.profileSearchButton){
            val intent = Intent(this@MainActivity, ProfileSearcherActivity::class.java)
            startActivity(intent)
        }

        return true
    }

}