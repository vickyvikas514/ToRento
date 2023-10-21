package com.example.torento
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.torento.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val SHARED_PREF:String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val char = LandingPage.character

        val db = Firebase.firestore
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signupText.setOnClickListener{
            val intent = Intent(this,SignIn::class.java)
            startActivity(intent)
            finish()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val userid = firebaseAuth.currentUser!!.uid




        binding.signupbtn.setOnClickListener{
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
                    "password" to pass
                )

                LandingPage.id = username
                if(char==1){
                    db.collection("owners").document(username)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d("vikas","DocumentSnapshot added with ID: $userid")
                        }
                        .addOnFailureListener{
                                e-> Log.w("vikas", "Error adding document",e)
                        }
                }else{
                    db.collection("users").document(username)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d("vikas","DocumentSnapshot added with ID: $userid")
                        }
                        .addOnFailureListener{
                                e-> Log.w("vikas", "Error adding document",e)
                        }
                }
                if (pass == confpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val sharedPreferences:SharedPreferences = getSharedPreferences(
                                SHARED_PREF, MODE_PRIVATE)
                            val editor:SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("name","true")
                            editor.apply()

                            val intent = Intent(this, owner_home_activity::class.java)
                            startActivity(intent)
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