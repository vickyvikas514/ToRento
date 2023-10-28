package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.ProgressBar

import android.widget.Toast
import com.example.torento.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {
    companion object {

        lateinit var id: String
    }

    private lateinit var progress: ProgressBar

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val SHARED_PREF: String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        progress = ProgressBar(this)


        val db = Firebase.firestore
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signupText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
        firebaseAuth = FirebaseAuth.getInstance()





        binding.signupbtn.setOnClickListener {

            //  progress.visibility = View.VISIBLE

            val name = binding.name.text.toString()
            val username = binding.username.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confpass = binding.confpass.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confpass.isNotEmpty()) {
                val user = hashMapOf(
                    "name" to name,
                    "username" to username,
                    "phone" to phone,
                    "email" to email,
                    "password" to pass,
                    "imageuri" to "temp"
                )
                if (username != null) {
                    id = username
                }


                db.collection("users").document(id)
                    .set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User is set", Toast.LENGTH_SHORT).show()
                        Log.d("vikas", "DocumentSnapshot added with ID: $")
                    }
                    .addOnFailureListener { e ->
                        Log.w("vikas", "Error adding document", e)
                    }

                if (pass == confpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val sharedPreferences: SharedPreferences = getSharedPreferences(
                                SHARED_PREF, MODE_PRIVATE
                            )
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("name", "true")
                            editor.putString("username", id)
                            editor.apply()

                            val intent = Intent(this, owner_home_activity::class.java)
                            startActivity(intent)
                            progress.visibility = View.GONE

                            finish()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }
        }
    }


}


