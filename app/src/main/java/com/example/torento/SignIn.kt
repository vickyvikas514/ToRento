package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.torento.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignIn : AppCompatActivity() {
    //sirf ppp waala hai username ka docref dekhna hai
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth : FirebaseAuth
    val SHARED_PREF:String = "sharedPrefs"
    private var db = Firebase.firestore
    private var Regemail:String="temp"
    
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
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                emailcheck(username)
                delay(2000)
                loginbtn(username,email,pass)
            }


        }

    }
    private fun emailcheck(username: String) {
        val docref = db.collection("users").document(username)
        if (docref != null) {
            docref.get().addOnSuccessListener {
                if (it != null) {
                    Toast.makeText(this,"Regmail is initializing",Toast.LENGTH_SHORT).show()
                    Regemail = it.data?.get("email").toString()
                } else {
                    Toast.makeText(this, "Fail!!", Toast.LENGTH_SHORT).show()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed!!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "DocRef is NULL", Toast.LENGTH_SHORT).show()
        }

    }
    private fun loginbtn(username: String, email: String, pass: String) {
             if(email.isNotEmpty() && pass.isNotEmpty() && username.isNotEmpty() && Regemail==email){
            firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                if(it.isSuccessful){
                    val sharedPreferences:SharedPreferences = getSharedPreferences(
                        SHARED_PREF, MODE_PRIVATE
                    )
                    val editor:SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("name","true")
                    editor.putString("username",username)
                    editor.apply()
                    val intent = Intent(this,user_home_activity::class.java)
                    startActivity(intent)
                    finish()
                } else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }else if(Regemail!=email){
            Toast.makeText(this,"$Regemail provide correct details, Your username and email are not attached to the same account",Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"Please provide all the details",Toast.LENGTH_SHORT).show()
        }
    }
    private fun remember(){
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            SHARED_PREF, MODE_PRIVATE
        )
        val check:String?=sharedPreferences.getString("name","")
        if(check.equals("true")){
            val intent = Intent(this,user_home_activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}