package com.example.torento.LOGIN

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.Global
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.torento.databinding.ActivitySignUpBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.R
import com.example.torento.USER.user_home_activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
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
        private const val RC_SIGN_IN = 9001
    }
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val SHARED_PREF: String = "sharedPrefs"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      // val db = Firebase.firestore
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        job = Job()
        binding.signupText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }


        binding.phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 10) {
                    hideKeyboard(this@SignUp)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 10) {
                    // Truncate the input to 10 digits if exceeded
                    binding.phone.setText(s?.subSequence(0, 10))
                    binding.phone.setSelection(10) // Move cursor to the end
                }
            }
        })

        binding.signupbtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            hideKeyboard(this@SignUp)
            val name = binding.name.text.toString()
            val username = binding.username.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confpass = binding.confpass.text.toString()
            if (name.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confpass.isNotEmpty()) {
                if (phone.length != 10) {
                    Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                } else if (!isValidPassword(pass)) {
                    Toast.makeText(this, "Password must contain at least 1 lowercase, 1 uppercase, 1 digit, and 1 special character.", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                } else if (confpass != pass) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                } else {
                    GlobalScope.launch {
                        loginlate(pass, confpass, email, name, username, phone)
                    }
                    showPopup()
                }
            } else {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set the Google Sign-In button listener
        binding.GoogleLogin.setOnClickListener {
            signIn()
        }


    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
           handleSignResult(task)
        }
    }

    private fun handleSignResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("SignUp", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    //binding.progressBar.visibility = View.GONE
                    showUserInfoPopup()
                    //TODO showing layout for adding the users info to firestore
                    // Navigate to your next activity
                } else {
                    Toast.makeText(this, "Googel sign in failed", Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                }
            }
    }
    @SuppressLint("MissingInflatedId")
    private fun showUserInfoPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.googlesigninuser, null)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        val nameInput = popupView.findViewById<TextInputEditText>(R.id.name)
        val usernameInput = popupView.findViewById<TextInputEditText>(R.id.username)
        val phoneInput = popupView.findViewById<TextInputEditText>(R.id.phone)
       val submitButton = popupView.findViewById<Button>(R.id.submit_button)

        submitButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val phone = phoneInput.text.toString()
            val name = nameInput.text.toString()



            if (username.isNotEmpty() && phone.isNotEmpty() && name.isNotEmpty() ) {
                GlobalScope.launch( Dispatchers.IO){
                    saveUserInfo(name, username, phone)

                }
                popupWindow.dismiss()

            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserInfo(name: String, username: String, phone: String) {
        val user = firebaseAuth.currentUser ?: return
        val uid = user.uid
        val email  = user.email
        val sharedPreferences:SharedPreferences = getSharedPreferences(
            SHARED_PREF, MODE_PRIVATE
        )
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("name","true")
        editor.putString("username",email)
        editor.putString("usertype", LandingPage.usertype)
        editor.apply()
        val userMap = hashMapOf(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "email" to email,
            "usertype" to LandingPage.usertype
        )


        Firebase.firestore.collection("users").document(email!!)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                changeThePage()
                // Navigate to the next activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
    private fun isValidPassword(password: String): Boolean {
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasLowercase && hasUppercase && hasDigit && hasSpecialChar
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
                    if (email != null) {
                        id = email
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
        val user = firebaseAuth.currentUser ?: return
        val uid = user.uid
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
    private fun changeThePage(){
        if (LandingPage.usertype == "user") {
            val intent = Intent(this, user_home_activity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }
    }

}


