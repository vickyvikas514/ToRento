package com.example.torento.COMMON

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.torento.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    val SHARED_PREF:String = "sharedPrefs"
    private var db = com.google.firebase.ktx.Firebase.firestore
    private var storageRef = Firebase.storage
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
       binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = Firebase.firestore
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username" , "")
        val docRefUser = userkey?.let { it1 -> db.collection("users").document(it1) }

        GlobalScope.launch (Dispatchers.IO){
            if (docRefUser != null) {
                retreivingdataBG(userkey.toString())
            }

        }
        binding.UpdateBtn.setOnClickListener{
            val name = binding.name.text.toString()

            val phone = binding.phone.text.toString()



            if (name.isNotEmpty() && phone.isNotEmpty() ) {
                val updateData = hashMapOf(
                    "name" to name,
                    "phone" to phone,
                )
                if (docRefUser != null) {
                    docRefUser.update(updateData as Map<String, Any>)
                        .addOnSuccessListener {
                            val intent = Intent(this, Profile::class.java)
                            startActivity(intent)
                            finish()
                            Toast.makeText( this,"Success", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText( this,"failure", Toast.LENGTH_SHORT).show()
                        }
                }

            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }

        }

        val galleryimage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.dp.setImageURI(it)
                if (it != null) {
                    if (userkey != null) {
                        Log.d("jiji","1")
                        binding.progressBar.visibility = View.VISIBLE
                        upload(userkey,it)
                    }
                    //
                }
            }
        )
        binding.dpupdate.setOnClickListener {
            galleryimage.launch("image/*")
        }

    }
    private fun upload(userkey: String,uri: Uri){
        GlobalScope.launch(Dispatchers.IO){
            try {
                Log.d("jiji","2")
                uploadBG(userkey,uri)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    suspend fun uploadBG(userkey: String,uri: Uri?){
        Log.d("jiji","3")
        storageRef = FirebaseStorage.getInstance()
        //it takes time and check for uri!=null
        if (uri != null) {
            Log.d("jiji","4")
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri)
                .addOnSuccessListener {
                    Log.d("jiji","5")
                    //Toast.makeText(this, "Part-1", Toast.LENGTH_SHORT).show()
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { imageuri ->
                            // Log.d("jiji","6")
                            //Toast.makeText(this, "Part-2", Toast.LENGTH_SHORT).show()
                            // val imageUrl = imageuri.toString()
                            val docRefUser = userkey?.let { it1 ->
                                db.collection("users").document(
                                    it1
                                )
                            }

                            val updateData = hashMapOf(
                                "imageuri" to imageuri,

                                )
                            if (docRefUser != null) {
                                docRefUser.update(updateData as Map<String, Any>)
                                    .addOnSuccessListener {

                                        binding.progressBar.visibility = View.GONE
                                        //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show()
                                    }
                            }


                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Part-3", Toast.LENGTH_SHORT).show()
                }
        }

    }
    suspend fun retreivingdataBG(userKey: String){
        Log.d("gum","${userKey}")
        val roomRef = userKey?.let { it1 -> db.collection("users").document(it1) }
        if (userKey != null) {
            if (roomRef != null) {
                roomRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val roomData = documentSnapshot.data
                            Log.d("gum","1")
                            // Update EditText fields with existing data
                            GlobalScope.launch (Dispatchers.Main){
                                updateEditTextFields(roomData)
                            }

                        } else {
                            Toast.makeText(this@UpdateActivity, "Document not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@UpdateActivity, "Failed to fetch data: $e", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }
    private fun updateEditTextFields(personalData: Map<String, Any>?) {
        Log.d("gum","2")
        if (personalData != null) {
            // Example: Update length EditText field
            val length = personalData["name"] as? String
            binding.name.setText(length)
            binding.phone.setText(personalData["phone"] as? String)
            val setimageuri = personalData["imageuri"] as? String
            if (setimageuri != "temp") {
                Glide.with(this)
                    .load(setimageuri)
                    .into(binding.dp)
            }
        }
    }
}