package com.example.chatroomapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private lateinit var edtLoginEmail : EditText
    private lateinit var edtLoginPassword : EditText

    private lateinit var edtName : EditText
    private lateinit var edtRegisterEmail : EditText
    private lateinit var edtRegisterPassword : EditText
    private lateinit var edtConfirmPassword : EditText

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser
        if (user != null) {
            // User is signed in
            val i = Intent(this@LoginActivity, CategoryActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
        }

        loginButton = findViewById(R.id.cirLoginButton)
        registerButton = findViewById(R.id.cirRegisterButton)

        edtLoginEmail = findViewById(R.id.editLoginEmail)
        edtLoginPassword = findViewById(R.id.editLoginPassword)

        edtName = findViewById(R.id.edtName)
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail)
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)

        loginButton.setOnClickListener {
            val email = edtLoginEmail.text.toString()
            val password = edtLoginPassword.text.toString()

            if(email.isNotEmpty() || password.isNotEmpty()){
                login(email, password)
            }
            else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtRegisterEmail.text.toString()
            val password = edtRegisterPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if(name.isNotEmpty() || email.isNotEmpty() || password.isNotEmpty() || confirmPassword.isNotEmpty()){
                register(name, email, password, confirmPassword)
            }
            else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun register(name: String, email: String, password: String, confirmPassword: String){
        if(password == confirmPassword){
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!)
                    Toast.makeText(this,"Successfully registered, Kindly check for verification email",Toast.LENGTH_SHORT).show()
                    mAuth.currentUser!!.sendEmailVerification()
                    //val intent = Intent(this@LoginActivity, CategoryActivity::class.java)
                    viewLoginClicked()
                    //startActivity(intent)
                    //finish()
                }
                else{
                    Toast.makeText(this@LoginActivity, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().reference
        mDbRef.child("users").child(uid).setValue(User(name, email, uid))
    }

    private fun login(email: String, password:String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if(task.isSuccessful){
                if(mAuth.currentUser?.isEmailVerified == true) {
                    val intent = Intent(this@LoginActivity, CategoryActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this,"Please verify your email.", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun viewRegisterClicked() {
        val loginLayout :View = findViewById(R.id.loginLayout)
        val registerLayout : View = findViewById(R.id.registerLayout)

        loginLayout.visibility = View.GONE
        registerLayout.visibility = View.VISIBLE
    }

    fun viewLoginClicked() {
        val loginLayout : View = findViewById(R.id.loginLayout)
        val registerLayout : View = findViewById(R.id.registerLayout)

        loginLayout.visibility = View.VISIBLE
        registerLayout.visibility = View.GONE
    }

   fun viewRegisterClicked(v : View) {
       val loginLayout :View = findViewById(R.id.loginLayout)
       val registerLayout : View = findViewById(R.id.registerLayout)

       loginLayout.visibility = View.GONE
       registerLayout.visibility = View.VISIBLE
    }

    fun viewLoginClicked(v : View) {
        val loginLayout : View = findViewById(R.id.loginLayout)
        val registerLayout : View = findViewById(R.id.registerLayout)

        loginLayout.visibility = View.VISIBLE
        registerLayout.visibility = View.GONE
    }
}
