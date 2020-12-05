package com.example.umessageapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registrar.*

class RegistrarActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)

        val toolbar: Toolbar = findViewById(R.id.toolbar_registrar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Registrar"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegistrarActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        btn_register.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val username: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if (username == ""){
            Toast.makeText(this@RegistrarActivity,"Escribir usuario", Toast.LENGTH_SHORT).show()
        }else if (email == ""){
            Toast.makeText(this@RegistrarActivity,"Escribir email", Toast.LENGTH_SHORT).show()
        }else if (password == ""){
            Toast.makeText(this@RegistrarActivity,"Escribir contraseña", Toast.LENGTH_SHORT).show()
        }else{
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"]= firebaseUserID
                        userHashMap["username"]= username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/umessagerapp.appspot.com/o/profile.png?alt=media&token=77d02d9f-64c6-4ec8-800e-9eb0245fdd83"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/umessagerapp.appspot.com/o/cover.jpg?alt=media&token=a9950cfe-e9f2-4f33-8487-ea3c20c4dfe4"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com/"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    val intent = Intent(this@RegistrarActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                    else
                    {
                        Toast.makeText(this@RegistrarActivity,"Error message: "+ task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}