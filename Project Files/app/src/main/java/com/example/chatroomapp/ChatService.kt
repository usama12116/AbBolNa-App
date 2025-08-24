package com.example.chatroomapp


import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson

class ChatService : Service() {

    private val mBinder: IBinder = MyBinder()
    private lateinit var chatList : ArrayList<com.example.chatroomapp.Message>
    private var category: String? = null
    private lateinit var mDbRef : DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var backgroundThread: Thread

    companion object {
        const val PREFS_NAME = "ChatPrefs"
        const val MAX_LOCAL_MESSAGES = 100
    }

    override fun onCreate() {
        super.onCreate()
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        chatList = ArrayList()

        loadChatMessages()
    }

    override fun onBind(intent: Intent?): IBinder {

        //Retrieve the category from the intent extras
        category = intent?.getStringExtra("category").toString()

        Log.d("Category", "background Category: $category")

        if (!category.isNullOrBlank()) {
            backgroundThread = Thread {
                mDbRef.child("chats").child(category!!).child("messages")
                    .addValueEventListener(createValueEventListener(category!!))
            }
            backgroundThread.start()
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
        fun getService(): ChatService {
            return this@ChatService
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

                if (newMessages.isNotEmpty()) {
                    // Notify callback with the latest messages
                    callback?.onDataChanged(newMessages)

                    chatList.clear()

                    // Update local chatList with new messages
                    chatList.addAll(newMessages)
                    // Trim local messages to keep only the last MAX_LOCAL_MESSAGES
                    if (chatList.size > MAX_LOCAL_MESSAGES) {
                        chatList = ArrayList(chatList.subList(chatList.size - MAX_LOCAL_MESSAGES, chatList.size))
                    }

                    // Save locally stored messages
                    saveChatMessages()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        }
    }

    private fun loadChatMessages() {
        Log.d("BackgroundService", "Loading chat messages from local storage")
        val prefs = getSharedPreferences(PREFS_NAME + "_" + category, Context.MODE_PRIVATE)
        val json = prefs.getString("chatList", null)

        chatList.clear()  // Clear the list before adding from local storage

        if (json != null) {
            val type = object : TypeToken<ArrayList<com.example.chatroomapp.Message>>() {}.type
            val loadedMessages = Gson().fromJson<ArrayList<com.example.chatroomapp.Message>>(json, type)

            Log.d("ChatList", "Loaded Chat Message: $loadedMessages")

            // Add loaded messages to the chatList
            chatList.addAll(loadedMessages)

            // Trim local messages to keep only the last MAX_LOCAL_MESSAGES
            if (chatList.size > MAX_LOCAL_MESSAGES) {
                chatList = ArrayList(chatList.subList(chatList.size - MAX_LOCAL_MESSAGES, chatList.size))
            }

            callback?.onDataChanged(chatList)
        }
    }

    fun getLocallyStoredMessages(): ArrayList<com.example.chatroomapp.Message> {
        Log.d("ChatList", "Locally Stored Chatlist content: $chatList") // Add this log statement
        return chatList
    }

    private fun saveChatMessages() {
        val prefs = getSharedPreferences(PREFS_NAME + "_" + category, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Trim local messages to keep only the last MAX_LOCAL_MESSAGES
        if (chatList.size > MAX_LOCAL_MESSAGES) {
            chatList = ArrayList(chatList.subList(chatList.size - MAX_LOCAL_MESSAGES, chatList.size))
        }

        val json = Gson().toJson(chatList)
        editor.putString("chatList", json)
        editor.apply()
    }

    // Callback interface for notifying the main thread about data changes
    interface Callback {
        fun onDataChanged(chatList: ArrayList<com.example.chatroomapp.Message>)
    }

    private var callback: Callback? = null

    // Method to set the callback listener
    fun setCallback(callback: Callback) {
        this.callback = callback
    }
}