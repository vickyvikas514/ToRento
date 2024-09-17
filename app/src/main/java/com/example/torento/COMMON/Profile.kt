package com.example.torento.COMMON

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Toast

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide

import com.example.torento.R

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
    private lateinit var dpuri:String



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")

        binding.edit.setOnClickListener {
            EditProfile()
        }
        binding.pphoto.setOnClickListener{
            showImageOptionsDialog()
        }
            Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()

        if (userkey != null) {
            set(userkey)
        }
        supportActionBar?.setTitle("KAMRE")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.brown)))



    }

    private fun set(userkey: String){
        lifecycleScope.launch(Dispatchers.IO){
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
    private fun EditProfile(){
        val intent = Intent(this, UpdateActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun updateUI(data: MutableList<String>) {
        if (data.size >= 5) {
            binding.name.text = data[0]
            binding.username.text = data[1]
            binding.phone.text = data[2]
            binding.email.text = data[3]

            val setimageuri = data[4]
            if (setimageuri != "") {
                Glide.with(this)
                    .load(setimageuri)
                    .into(binding.dp)
            }else
            {
                binding.dp.setImageResource(R.drawable.demodp)
            }
        } else {
            // Handle the case when the data list is empty or insufficient
            Log.e("Profile", "Invalid data list size: ${data.size}")
        }
    }

    private fun showImageOptionsDialog() {
        // Define options in an array
        val options = arrayOf("View Full Screen", "Change DP")

        // Create a dialog for options
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // View Full Screen option selected
                        viewImageFullScreen()
                    }
                    1 -> {
                        // Change DP option selected
                        EditProfile()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun viewImageFullScreen() {
        // Create an intent to open the full-screen activity
        val intent = Intent(this, FullScreenDPView::class.java)
        intent.putExtra("imageUri", dpuri.toString()) // Pass the image URI to the activity
        startActivity(intent)
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
                   dpuri = it["imageuri"].toString()
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

}