package com.example.chatroomapp

import UserListAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileSearcherActivity : AppCompatActivity() {

    private lateinit var etSearchName: EditText
    private lateinit var btnSearch: Button
    private lateinit var listView: ListView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_searcher)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        etSearchName = findViewById(R.id.etSearchName)
        btnSearch = findViewById(R.id.btnSearch)
        listView = findViewById(R.id.listView)

        btnSearch.setOnClickListener {
            val searchQuery = etSearchName.text.toString().trim()

            if (searchQuery.isNotEmpty()) {
                searchUser(searchQuery)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchUser(searchQuery: String) {
        val query: Query = mDbRef.child("users").orderByChild("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: MutableList<User> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                displaySearchResults(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ProfileSearcherActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displaySearchResults(userList: List<User>) {
        val adapter = UserListAdapter(this, userList)
        listView.adapter = adapter
    }
}
