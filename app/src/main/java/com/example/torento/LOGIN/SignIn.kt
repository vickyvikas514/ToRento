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
import android.widget.TextView
import android.widget.Toast
import com.example.torento.databinding.ActivitySignInBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.R
import com.example.torento.USER.user_home_activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var googleSignInClient: GoogleSignInClient
    companion object {
        var id: String = ""
        private const val RC_SIGN_IN = 9001
    }

    //TODO username le ke aa bina type kare ya phit home activity pe kaam kar ki username na laana pade

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
            hideKeyboard(this@SignIn)
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    emailcheck(username, email, pass)
                }
            }
        }
       val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set the Google Sign-In button listener
        binding.GoogleLogin.setOnClickListener {
            signOutAndSignIn()
            Toast.makeText(this@SignIn, "1", Toast.LENGTH_SHORT).show()
        }
    }
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
    private fun emailcheck(username: String, email: String, pass: String) {
        val docref = db.collection("users").document(email)
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
                    loginbtn( email, pass)
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
    private fun loginbtn( email: String, pass: String) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.INVISIBLE
            if (task.isSuccessful) {
                val sharedPreferences:SharedPreferences = getSharedPreferences(
                    SHARED_PREF, MODE_PRIVATE
                )
                val editor:SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("name","true")
                editor.putString("username",email)
                editor.putString("usertype", LandingPage.usertype)
                editor.apply()
                // Your existing code for successful sign-in remains the same
                changeThePage(email)
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
    private fun signOutAndSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            googleSignInClient.revokeAccess().addOnCompleteListener(this) {
                signIn()
            }
        }
    }
    private fun signIn() {
        Toast.makeText(this@SignIn, "2", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this@SignIn, "3", Toast.LENGTH_SHORT).show()
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            val email = account.email
            firebaseAuthWithGoogle(account.idToken!!, email)
        } catch (e: ApiException) {
            Log.w("SignUp", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String?) {
        Toast.makeText(this@SignIn, "4", Toast.LENGTH_SHORT).show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    //binding.progressBar.visibility = View.GONE

                    val sharedPreferences:SharedPreferences = getSharedPreferences(
                        SHARED_PREF, MODE_PRIVATE
                    )
                    val editor:SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("name","true")
                    editor.putString("username",email)
                    editor.putString("usertype", LandingPage.usertype)
                    editor.apply()
                    if (email != null) {
                        changeThePage(email)
                    }
                    //TODO showing layout for adding the users info to firestore
                    // Navigate to your next activity
                } else {
                    Toast.makeText(this, "Googel sign in failed", Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                }
            }
    }
    private fun changeThePage(userkey: String){
        val docref = userkey?.let { db.collection("users").document(it) }
        docref?.get()?.addOnSuccessListener { document ->
            regusertype = document.getString("usertype") ?: "temp"
            Toast.makeText(this@SignIn, regusertype, Toast.LENGTH_SHORT).show()
            Toast.makeText(this@SignIn, "5", Toast.LENGTH_SHORT).show()
            if(LandingPage.usertype !=regusertype){
                Toast.makeText(this,"User can't exist",Toast.LENGTH_SHORT)
            }
            else if(LandingPage.usertype =="tenant"){
                Toast.makeText(this@SignIn, "7", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignIn, user_home_activity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SignIn, "6", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignIn, owner_home_activity::class.java)
                startActivity(intent)
                finish()
            }
        }
            ?.addOnFailureListener{
                Toast.makeText(this, "NOWAY", Toast.LENGTH_SHORT).show()
                Log.e("SignInChangePage", "Error checking email: ${it.message}")
            }

    }
}