package com.example.torento.COMMON

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.torento.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    val SHARED_PREF:String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
       binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = Firebase.firestore
        binding.UpdateBtn.setOnClickListener{
            val name = binding.name.text.toString()

            val phone = binding.phone.text.toString()

            val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
            val userkey: String? = sharedPreferences.getString("username" , "")
            val docRefUser = userkey?.let { it1 -> db.collection("users").document(it1) }
            if (name.isNotEmpty() && phone.isNotEmpty() ) {
                 val updateData = hashMapOf(
                    "name" to name,
                    "phone" to phone,
                )
                if (docRefUser != null) {
                    docRefUser.update(updateData as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText( this,"Success", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText( this,"failure", Toast.LENGTH_SHORT).show()
                        }
                }

            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }

    }
}