package com.example.torento.LOGIN

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.torento.databinding.ActivitySignInBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.R
import com.example.torento.USER.user_home_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth : FirebaseAuth
    val SHARED_PREF:String = "sharedPrefs"
    private var db = Firebase.firestore
    private var regusertype:String="temp"
    private lateinit var popupWindow: PopupWindow


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        remember()

        binding.signupText.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }



        binding.loginButton.setOnClickListener{
            binding.progressBar.visibility = View.VISIBLE
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    emailcheck(username, email, pass)
                }
            }
        }
       // Toast.makeText(this@SignIn, firebaseAuth.currentUser?.uid ?.toString(), Toast.LENGTH_SHORT).show()

    }
    private fun emailcheck(username: String, email: String, pass: String) {
        val docref = db.collection("users").document(username)
        docref.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val regemail = document.getString("email")
                regusertype = document.getString("usertype") ?: "temp"
                if (regemail != null && regemail == email) {
                    /*if (firebaseAuth.currentUser?.isEmailVerified == true) {

                    } else {
                        showPopup("Please verify your email first")
                        binding.progressBar.visibility = View.GONE
                    }*/
                    loginbtn(username, email, pass)
                    binding.progressBar.visibility = View.GONE
                } else {
                    showPopup("Username and email are not associated")
                    binding.progressBar.visibility = View.GONE
                }
            } else {
                showPopup("User not found")
                binding.progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            Log.e("SignIn", "Error checking email: ${it.message}")
            showPopup("Failed to check email")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun loginbtn(username: String, email: String, pass: String) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.INVISIBLE
            if (task.isSuccessful) {
                // Your existing code for successful sign-in remains the same
                val sharedPreferences:SharedPreferences = getSharedPreferences(
                    SHARED_PREF, MODE_PRIVATE
                )
                val editor:SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("name","true")
                editor.putString("username",username)
                editor.putString("usertype", LandingPage.usertype)
                editor.apply()
                if(LandingPage.usertype !=regusertype){
                    Toast.makeText(this,"User can't exist",Toast.LENGTH_SHORT)
                }
                else if(LandingPage.usertype =="tenant"){
                    val intent = Intent(this, user_home_activity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, owner_home_activity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this@SignIn, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showPopup(message: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup, null)

        val popuptext = view.findViewById<TextView>(R.id.popuptext)
        popuptext.text = message

        popupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        Handler().postDelayed({
            popupWindow.dismiss()
        }, 2000)
    }
   /* private fun emailcheck(username: String,email: String,pass: String) {
        val docref = db.collection("users").document(username)
        if (docref != null) {
            docref.get().addOnSuccessListener {
                if (it != null) {
                    Toast.makeText(this,"Regmail is initializing",Toast.LENGTH_SHORT).show()
                    val Regemail = it.data?.get("email").toString()
                    regusertype = it.data?.get("usertype").toString()
                    loginbtn(username,email,pass,Regemail)
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

    }*/
    /*private fun loginbtn(username: String, email: String, pass: String,Regemail:String) {
            val user = firebaseAuth.currentUser
        if (user != null) {
            if(email.isNotEmpty() && pass.isNotEmpty() && username.isNotEmpty() && Regemail==email && user.isEmailVerified){
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                    binding.progressBar.visibility = View.INVISIBLE
                    if(it.isSuccessful){
                        val sharedPreferences:SharedPreferences = getSharedPreferences(
                            SHARED_PREF, MODE_PRIVATE
                        )
                        val editor:SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString("name","true")
                        editor.putString("username",username)
                        editor.putString("usertype", LandingPage.usertype)
                        editor.apply()
                        if(LandingPage.usertype !=regusertype){
                            Toast.makeText(this,"User can't exist",Toast.LENGTH_SHORT)
                        }
                        else if(LandingPage.usertype =="tenant"){
                            val intent = Intent(this, user_home_activity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            val intent = Intent(this, owner_home_activity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else{
                        Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }else if(Regemail!=email){
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this,"$Regemail provide correct details, Your username and email are not attached to the same account",Toast.LENGTH_SHORT).show()
            }
            else if(user.isEmailVerified == false){
                showPopup()
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this,"Please verify your email",Toast.LENGTH_SHORT).show()
            }else{
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this,"Please provide all the details",Toast.LENGTH_SHORT).show()
            }
        }
    }*/
    private fun remember(){
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            SHARED_PREF, MODE_PRIVATE
        )
        val check:String?=sharedPreferences.getString("name","")
        if(check.equals("true")){
            if(LandingPage.usertype =="tenant"){
                val intent = Intent(this, user_home_activity::class.java)
                startActivity(intent)
                // progress.visibility = View.GONE

                finish()
            }else{
                val intent = Intent(this, owner_home_activity::class.java)
                startActivity(intent)
                //  progress.visibility = View.GONE

                finish()
            }
        }
    }
}