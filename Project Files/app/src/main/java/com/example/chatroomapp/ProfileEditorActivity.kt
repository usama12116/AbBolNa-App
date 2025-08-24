package com.example.chatroomapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileEditorActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var etNewName: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnSaveChanges: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_editor)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        etNewName = findViewById(R.id.etNewName)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        btnSaveChanges.setOnClickListener {
            val newName = etNewName.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            val user = mAuth.currentUser

            if (newName.isNotEmpty()) {
                user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(newName).build())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            mDbRef.child("users").child(user.uid).child("name").setValue(newName)
                            Toast.makeText(
                                this@ProfileEditorActivity,
                                "Name updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ProfileEditorActivity,
                                "Failed to update name",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

            if (newPassword.isNotEmpty()) {
                user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ProfileEditorActivity,
                            "Password updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ProfileEditorActivity,
                            "Failed to update password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
