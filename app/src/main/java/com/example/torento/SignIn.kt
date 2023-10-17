package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.torento.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth : FirebaseAuth
    val SHARED_PREF:String = "sharedPrefs"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        remember()

        binding.signupText.setOnClickListener{
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener{
            val email = binding.username.text.toString()
            val pass = binding.password.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                    if(it.isSuccessful){
                        val sharedPreferences:SharedPreferences = getSharedPreferences(
                            SHARED_PREF, MODE_PRIVATE
                        )
                        val editor:SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString("name","true")
                        editor.apply()
                        val intent = Intent(this,owner_home_activity::class.java)
                        startActivity(intent)
                        finish()
                    } else{
                        Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Please provide all the details",Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun remember(){
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            SHARED_PREF, MODE_PRIVATE
        )
        val check:String?=sharedPreferences.getString("name","")
        if(check.equals("true")){
            val intent = Intent(this,owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}