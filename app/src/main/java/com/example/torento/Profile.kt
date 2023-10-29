package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.torento.databinding.ActivityProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    val SHARED_PREF: String = "sharedPrefs"
    private lateinit var namei: TextView
    private var db = Firebase.firestore
    private var storageRef = Firebase.storage
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        namei = findViewById(R.id.name)
        super.onCreate(savedInstanceState)

        binding.back.setOnClickListener {
            val intent = Intent(this, owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        val galleryimage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.dp.setImageURI(it)
                if (it != null) {
                    uri = it
                }
            }
        )
        binding.dpupdate.setOnClickListener {
            galleryimage.launch("image/*")
        }
        binding.edit.setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java)
            startActivity(intent)
            finish()
        }
        if (userkey == "vik") {
            Toast.makeText(this, "YES", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()
        }
        if (userkey != null) {
            set(userkey)
        }
        /* Upload image to storage and firestore*/
        storageRef = FirebaseStorage.getInstance()
        binding.dpupload.setOnClickListener {
            //it takes time and check for uri!=null
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Part-1", Toast.LENGTH_SHORT).show()
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { imageuri ->
                            Toast.makeText(this, "Part-2", Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
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

        /* upload*/

    }

    fun set(userkey: String) {

        val docref = db.collection("users").document(userkey)
        if (docref != null) {
            docref.get().addOnSuccessListener {
                if (it != null) {
                    val name = it.data?.get("name")?.toString()
                    if (name == null) {
                        Toast.makeText(this, "FAIL", Toast.LENGTH_SHORT).show()
                    } else {
                        namei.text = name
                    }

                    binding.username.text = it.data?.get("username")?.toString()

                    binding.phone.text = it.data?.get("phone")?.toString()

                    binding.email.text = it.data?.get("email")?.toString()

                    val setimageuri = it.data?.get("imageuri")?.toString()
                    if (setimageuri != "temp") {
                        Glide.with(this)
                            .load(setimageuri)//check here , not checked yet
                            .into(binding.dp)
                    }

                } else {
                    Toast.makeText(this, "Fail!!", Toast.LENGTH_SHORT).show()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed!!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "VIKAS CHAUDHARY", Toast.LENGTH_SHORT).show()
        }


    }
}