package com.example.torento.LOGIN

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.airbnb.lottie.LottieAnimationView
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth : FirebaseAuth
    val SHARED_PREF:String = "sharedPrefs"
    private var db = Firebase.firestore
    private var regusertype:String="temp"
    private lateinit var popupWindow: PopupWindow
    private lateinit var googleSignInClient: GoogleSignInClient
    private var dpuri: Uri = "".toUri()
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var touchInterceptor:View
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
        loadingAnimation = binding.progressBar
        touchInterceptor = binding.touchInterceptor
        binding.signupText.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
         binding.loginButton.setOnClickListener{
            showProgressOverlay(true)
            hideKeyboard(this@SignIn)

            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if ( email.isNotEmpty() && pass.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    emailcheck( email, pass)
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
    private fun emailcheck( email: String, pass: String) {
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

                } else {
                    showPopup("Username and email are not associated")
                    showProgressOverlay(false)
                }
            } else {
                showPopup("User not found")
               showProgressOverlay(false)
            }
        }.addOnFailureListener {
            Log.e("SignIn", "Error checking email: ${it.message}")
            showPopup("Failed to check email")
            showProgressOverlay(false)
        }
    }
    private fun loginbtn( email: String, pass: String) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            showProgressOverlay(false)
            if (task.isSuccessful) {
                val sharedPreferences:SharedPreferences = getSharedPreferences(
                    SHARED_PREF, MODE_PRIVATE
                )
                val editor:SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("name","true")
                editor.putString("username",email)
                editor.putString("usertype", LandingPage.usertype)
                editor.putBoolean("signUpComplete", false)
                editor.apply()
                // Your existing code for successful sign-in remains the same
                changeThePage(email)
                showProgressOverlay(false)
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
                    val user = firebaseAuth.currentUser
                    // Sign in success
                    //binding.progressBar.visibility = View.GONE

                    val docRef = db.collection("users").document(user?.email.toString())
                    docRef.get().addOnSuccessListener { document ->
                        if(document.exists()){
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
                        }else{
                            showUserInfoPopup()
                        }
                    }.addOnFailureListener{
                        Toast.makeText(this, "Failed to get user data", Toast.LENGTH_SHORT).show()
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
                Dialogforwrongusertype("Your account is not associated with the selected usertype")
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
                Dialogforwrongusertype("User is not existed in database")
                Log.e("SignInChangePage", "Error checking email: ${it.message}")
            }

    }
    private fun Dialogforwrongusertype(msg : String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Wrong Usertype")
            .setMessage(msg)
            .setCancelable(true) // Allows dismissing the dialog by tapping outside or pressing the back button
            .create()

        dialog.show()
    }
    override fun onPause() {
        super.onPause()
        updateSharedPreferences()
    }

    override fun onStop() {
        super.onStop()
        updateSharedPreferences()
    }

    private fun updateSharedPreferences() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            SHARED_PREF, MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("signUpComplete", false) // or any other value you want to set
        editor.apply()
    }

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
        val dpspace = popupView.findViewById<MaterialCardView>(R.id.pphoto)
        val dp  = popupView.findViewById<ImageView>(R.id.dp)

        phoneInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 10) {
                    hideKeyboard(phoneInput)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 10) {
                    // Truncate the input to 10 digits if exceeded
                    phoneInput.setText(s?.subSequence(0, 10))
                    phoneInput.setSelection(10) // Move cursor to the end
                }
            }
        })
        dpspace.setOnClickListener {
            showImageSourceOptions()
        }
        submitButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val phone = phoneInput.text.toString()
            val name = nameInput.text.toString()



            if (username.isNotEmpty() && phone.isNotEmpty() && name.isNotEmpty() ) {
                if(phone.length != 10){
                    Toast.makeText(this, "Please provide 10 digit phone number", Toast.LENGTH_SHORT).show()
                }else{
                    GlobalScope.launch( Dispatchers.IO){
                        saveUserInfo(name, username, phone)
                    }
                    popupWindow.dismiss()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showImageSourceOptions() {
        // Define options in an array
        val options = arrayOf("Choose from Gallery", "Take Photo")

        // Create a dialog for options
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Choose from Gallery option selected
                        chooseFromGallery()
                    }
                    1 -> {
                        // Take Photo option selected
                        takePhoto()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
            "usertype" to LandingPage.usertype,
            "imageuri" to dpuri,
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
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun chooseFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        resultLauncherGallery.launch(galleryIntent)
    }
    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(cameraIntent)
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { handleImageBitmap(it) }
        } else {
            Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private val resultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            // Handle the selected image URI accordingly
            uri?.let { handleImageUri(it) }
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleImageBitmap(bitmap: Bitmap) {
        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        val uri = getImageUri(bitmap)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.googlesigninuser, null)
        val dp  = popupView.findViewById<ImageView>(R.id.dp)
        dp.setImageURI(uri)
        if (uri != null) {

            Log.d("jiji","1")
            //binding.progressBar.visibility = View.VISIBLE
            dpuri = uri

            //
        }
    }
    private fun handleImageUri(uri: Uri) {

        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.googlesigninuser, null)
        val dp  = popupView.findViewById<ImageView>(R.id.dp)
        dp.setImageURI(uri)
        if (uri != null) {
            Log.d("jiji","1")
            //binding.progressBar.visibility = View.VISIBLE
            dpuri = uri
            //
        }
    }
    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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
    private fun showProgressOverlay(show: Boolean) {
        //binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isClickable = show
        binding.root.isFocusable = show
        if (show) startAnimation() else stopAnimation()
    }
    private fun startAnimation() {
        touchInterceptor.visibility = View.VISIBLE
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
    }
    private fun stopAnimation() {
        touchInterceptor.visibility = View.INVISIBLE
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.INVISIBLE
    }
}