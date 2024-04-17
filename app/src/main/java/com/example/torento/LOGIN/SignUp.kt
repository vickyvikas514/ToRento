package com.example.torento.LOGIN

import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.torento.databinding.ActivitySignUpBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.R
import com.example.torento.USER.user_home_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {

    private lateinit var job: Job
    private lateinit var popupWindow: PopupWindow
    companion object {
        var id: String = ""
    }
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val SHARED_PREF: String = "sharedPrefs"

    private fun showPopup() {
        // Check if the activity is finishing or has been destroyed
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Your email is not verified, check your mails to verify the email")
                .setPositiveButton("I verified my email") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                    // Refresh the activity
                    restartApp(this@SignUp)
                }
                .setNegativeButton("Send email verification link again") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                    // Resend the verification email
                    sendEmailVerification()
                }
                .setCancelable(false) // Prevent dismiss on outside touch or back button

            // Create and show the dialog using the activity's context
            val dialog = builder.create()
            dialog.show()
        }

    }
    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }


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
            hideKeyboard(this@SignUp)
            val name = binding.name.text.toString()
            val username = binding.username.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confpass = binding.confpass.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confpass.isNotEmpty()) {

                GlobalScope.launch() {

                    loginlate(pass,confpass, email,name,username,phone)

                   // delay(5000)

                }
                showPopup()

            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the job when the activity is destroyed
        job.cancel()
    }


    private fun sendEmailVerification(){
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener{task->
                if(task.isSuccessful){
                    Toast.makeText(baseContext, "Verification email sent.",Toast.LENGTH_SHORT).show()

                } else{
                    Toast.makeText(baseContext, "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun loginlate(pass: String,confpass:String,email: String,name:String,username: String,phone: String){
        if (pass == confpass) {
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    sendEmailVerification()
                    val sharedPreferences: SharedPreferences = getSharedPreferences(
                        SHARED_PREF, MODE_PRIVATE
                    )
                    if (username != null) {
                        id = username
                    }
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        //editor.putString("name", "true")
                    editor.putString("username", id)
                    editor.putString("usertype", LandingPage.usertype)
                    editor.apply()
                    runOnUiThread {
                        Toast.makeText(this@SignUp, "Account created successfully", Toast.LENGTH_SHORT).show()
                    }
                    GlobalScope.launch (Dispatchers.IO){
                        writeUserToFirestore(name,username,phone,email,pass)
                    }


                } else {
                    val exception = it.exception
                    if (exception is FirebaseAuthWeakPasswordException) {
                        // Handle weak password exception
                        runOnUiThread {
                            Toast.makeText(this@SignUp, "Please use a stronger password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@SignUp, exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                     }
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            }

        }

    }
    suspend fun writeUserToFirestore(name:String, username:String, phone:String, email:String, pass:String) {
         runOnUiThread {
             Toast.makeText(this@SignUp, "writing data", Toast.LENGTH_SHORT).show()
         }
        try {
            withContext(Dispatchers.IO) {
                val db = Firebase.firestore
                runOnUiThread {
                    Toast.makeText(this@SignUp, "writing data yes", Toast.LENGTH_SHORT).show()
                }
                    val user = hashMapOf(
                        "name" to name,
                        "username" to username,
                        "phone" to phone,
                        "email" to email,
                        "password" to pass,
                        "imageuri" to "temp",
                        "usertype" to LandingPage.usertype
                    )


            // Perform Firestore write operation on the IO thread
                db.collection("users").document(id)
                    .set(user)
                    .addOnSuccessListener { documentReference ->
                        Log.d("Firestore", "DocumentSnapshot written with ID: $")

                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding document", e)
                    }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@SignUp, "writing data no", Toast.LENGTH_SHORT).show()
            }
            Log.e("Firestore", "Exception: $e")
        }
    }

}


