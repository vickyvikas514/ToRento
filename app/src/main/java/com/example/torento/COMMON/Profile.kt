package com.example.torento.COMMON

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.torento.LOGIN.LandingPage
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.USER.user_home_activity
import com.example.torento.databinding.ActivityProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlinx.coroutines.tasks.await

class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    val SHARED_PREF: String = "sharedPrefs"
    private var db = Firebase.firestore
    private var storageRef = Firebase.storage
    private lateinit var uri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")

        binding.back.setOnClickListener {
           back()
        }

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

            Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()

        if (userkey != null) {
            set(userkey)
        }
        if (userkey != null) {
            upload(userkey)
        }


    }
    private fun upload(userkey: String){
        GlobalScope.launch(Dispatchers.IO){
            try {
                uploadBG(userkey)
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    private fun set(userkey: String){
        GlobalScope.launch(Dispatchers.IO){
            try {
                val data  = setBG(userkey)
               Log.d("chaudhary",data.size.toString())
                launch(Dispatchers.Main) {
                    updateUI(data)
                }
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(data: MutableList<String>) {
        if (data.size >= 5) {
            binding.name.text = data[0]
            binding.username.text = data[1]
            binding.phone.text = data[2]
            binding.email.text = data[3]

            val setimageuri = data[4]
            if (setimageuri != "temp") {
                Glide.with(this)
                    .load(setimageuri)
                    .into(binding.dp)
            }
        } else {
            // Handle the case when the data list is empty or insufficient
            Log.e("Profile", "Invalid data list size: ${data.size}")
        }
    }

    suspend fun uploadBG(userkey: String){
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
    }

   suspend fun setBG(userkey: String) : MutableList<String>{
       val list :MutableList<String> = mutableListOf()

       try {
           val docref = db.collection("users").document(userkey).get().await()
           if (docref != null) {
               docref.data?.let {
                   list.add(it["name"].toString())
                   list.add(it["username"].toString())
                   list.add(it["phone"].toString())
                   list.add(it["email"].toString())
                   list.add(it["imageuri"].toString())
               }
               Log.d("chaudhary1", list.size.toString())
                   }

            else {
               Toast.makeText(this, "DocRef is NULL", Toast.LENGTH_SHORT).show()
           }
       } catch (e:Exception){
           Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
       }

    return list

    }
    private fun back(){
        if(LandingPage.usertype=="tenant"){
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