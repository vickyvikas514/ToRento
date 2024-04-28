package com.example.torento.COMMON

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.torento.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.Exception

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    val SHARED_PREF:String = "sharedPrefs"
    private var db = com.google.firebase.ktx.Firebase.firestore
    private var storageRef = Firebase.storage
    private var userkey: String? = ""
    private var dpuri:Uri = "".toUri()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
       binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = Firebase.firestore
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username" , "")
        val docRefUser = userkey?.let { it1 -> db.collection("users").document(it1) }
        GlobalScope.launch (Dispatchers.IO){
            if (docRefUser != null) {
                retreivingdataBG(userkey.toString())
            }

        }
        binding.UpdateBtn.setOnClickListener {

            val name = binding.name.text.toString()
            val phone = binding.phone.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    userkey?.let { key ->
                        if (dpuri != "".toUri()) {
                            binding.progressBar.visibility = View.VISIBLE
                            // Wait for the upload to complete
                            uploadBG(key, dpuri)
                        }
                    }
                   // delay(2000)


                    val updateData = hashMapOf(
                        "name" to name,
                        "phone" to phone,

                        )
                    if (docRefUser != null) {
                        docRefUser.update(updateData as Map<String, Any>)
                            .addOnSuccessListener {
                                val intent = Intent(this@UpdateActivity, Profile::class.java)
                                startActivity(intent)
                                finish()
                                Toast.makeText(this@UpdateActivity, "Success", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@UpdateActivity, "failure", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
            } else {
                Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
            }

        }

        binding.dpupdate.setOnClickListener {
            showImageSourceOptions()
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
        binding.dp.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
               //binding.progressBar.visibility = View.VISIBLE
                dpuri = uri
            }
            //
        }
    }
    private fun handleImageUri(uri: Uri) {
        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        binding.dp.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
                //binding.progressBar.visibility = View.VISIBLE
                dpuri = uri
            }
            //
        }
    }
    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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
    private suspend fun uploadBG(userkey: String, uri: Uri?) {
        storageRef = FirebaseStorage.getInstance()
        if (uri != null) {
            val reference = storageRef.getReference("images").child(System.currentTimeMillis().toString())
            reference.putFile(uri).await() // Await the completion of the upload

            val downloadUrl = reference.downloadUrl.await() // Await download URL
            val docRefUser = db.collection("users").document(userkey)

            val updateData = hashMapOf("imageuri" to downloadUrl.toString())
            docRefUser.update(updateData as Map<String, Any>).await() // Await Firestore update
        }
    }
    /*private suspend fun uploadBG(userkey: String,uri: Uri?)= GlobalScope.async{
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
                                        Toast.makeText(this@UpdateActivity, "failure", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }
                .addOnFailureListener {

                        Toast.makeText(this@UpdateActivity, "Part-3", Toast.LENGTH_SHORT).show()


                }
        }

    }.await()*/
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