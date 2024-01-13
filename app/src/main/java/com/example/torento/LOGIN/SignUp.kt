package com.example.torento.LOGIN

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.torento.databinding.ActivitySignUpBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.USER.user_home_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {

    private lateinit var job: Job
    companion object {

        lateinit var id: String
    }

  //  private lateinit var progress: ProgressBar

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val SHARED_PREF: String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      // val db = Firebase.firestore
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signupText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        job = Job()
        binding.signupbtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val name = binding.name.text.toString()
            val username = binding.username.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confpass = binding.confpass.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confpass.isNotEmpty()) {

                GlobalScope.launch() {

                    loginlate(pass,confpass, email)
                    writeUserToFirestore(name,username,phone,email,pass)
                   // delay(5000)

                }


            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the job when the activity is destroyed
        job.cancel()
    }
    private fun loginlate(pass: String,confpass:String,email: String){
        if (pass == confpass) {
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    val sharedPreferences: SharedPreferences = getSharedPreferences(
                        SHARED_PREF, MODE_PRIVATE
                    )
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("name", "true")
                    editor.putString("username", id)
                    editor.putString("usertype", LandingPage.usertype)
                    editor.apply()


                } else {
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
        }

    }
    private fun changethepage() {
        binding.progressBar.visibility = View.INVISIBLE
        // Your code to handle the result after the background work is finished
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

     suspend fun writeUserToFirestore(name:String, username:String, phone:String, email:String, pass:String) {
        try {
            withContext(Dispatchers.IO) {
                val db = Firebase.firestore

                    val user = hashMapOf(
                        "name" to name,
                        "username" to username,
                        "phone" to phone,
                        "email" to email,
                        "password" to pass,
                        "imageuri" to "temp",
                        "usertype" to LandingPage.usertype
                    )
                    if (username != null) {
                        id = username
                    }

            // Perform Firestore write operation on the IO thread
                db.collection("users").document(id)
                    .set(user)
                    .addOnSuccessListener { documentReference ->
                        Handler().postDelayed({
                            changethepage()
                        },3000)

                        Log.d("Firestore", "DocumentSnapshot written with ID: $")

                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding document", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Exception: $e")
        }
    }

}


