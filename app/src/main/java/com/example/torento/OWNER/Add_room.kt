package com.example.torento.OWNER

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.torento.LOGIN.LandingPage.Companion.num
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class add_room : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var db = Firebase.firestore
    private var storageRef = Firebase.storage
    private lateinit var length:String
    private lateinit var width:String
    private lateinit var location:String
    private lateinit var loaction_description:String
    private lateinit var amount:String
    private lateinit var owner_name:String
    private lateinit var breif_description:String
    var count=0
    private var roomId:String=""
    val SHARED_PREF : String = "sharedPrefs"
    private var userkey:String? = ""
    private lateinit var dpuri:Uri
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private val imagesList = mutableListOf<Uri>()



    private fun uploadImagesToFirebaseStorage(userkey: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            for ((index, imageUri) in imagesList.withIndex()) {
                try {
                    val imageUrl = uploadImage(imageUri)
                    uploadingmultipleimagesonfirestore(imageUrl,userkey)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("Disha1",e.toString())
                        Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()
    private suspend fun uploadingmultipleimagesonfirestore(imageUrl: Uri, userkey: String?){
        val roomId = userkey + "${num - 1}"
        try {
            val roomsCollection = db.collection("Rooms")
            val ownercollection = userkey?.let { db.collection(it) }
            //"imageuri" to FieldValue.arrayUnion(imageUrl)
            roomsCollection.document(roomId).update("imageuri", FieldValue.arrayUnion(imageUrl)).await()
            ownercollection?.document(roomId)?.update("imageuri", FieldValue.arrayUnion(imageUrl))?.await()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@add_room, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("Disha3",e.toString())
                Toast.makeText(this@add_room, "Error updating image in Firestore: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun uploadRoom( ):Boolean {
        // Replace 'roomId' with the actual ID of the room document
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        location = binding.Locality.text.toString()
        loaction_description = binding.locationDescription.text.toString()
        amount = binding.amount.text.toString()
        owner_name = binding.OwnerName.text.toString()
        breif_description = binding.RoomDescription.text.toString()
        Log.d("chutiya",roomId)
        if (length.isEmpty() || width.isEmpty() || location.isEmpty() || loaction_description.isEmpty() || amount.isEmpty() || owner_name.isEmpty() || breif_description.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@add_room, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
            showDeleteRoomConfirmationDialog()
            return false
        }else{
            val room = hashMapOf(
                "length" to length,
                "width" to width,
                "location" to location,
                "location_detail" to loaction_description,
                "owner_name" to owner_name,
                "amount" to amount,
                "breif_description" to breif_description,

                )

            if (roomId != "temp") {
                try {
                    db.collection("Rooms").document(roomId).update(room as Map<String, Any>).await()
                    userkey?.let {
                        db.collection(it).document(roomId).update(room as Map<String, Any>).await()
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@add_room, "Updated", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@add_room,owner_home_activity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@add_room, "Error updating room: $e", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@add_room,owner_home_activity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@add_room, "Userkey is equal to temp", Toast.LENGTH_SHORT).show()
                }
            }

            return true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val collectionReference = db.collection("Rooms")
        collectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                // Get the count of documents in the collection
                num = querySnapshot.size()+1
            }
            .addOnFailureListener { e ->
                num=1
                Toast.makeText(this, "fail in room count", Toast.LENGTH_SHORT).show()
            }
        Log.d("terabaap","$num")
        storageRef = FirebaseStorage.getInstance()
        ///////////////////////

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username", "")
        roomId = userkey + "${num - 1}"
        ///////////////////////
        val pickImages =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage(userkey)
                }
            }
        binding.uploadbtn.setOnClickListener {
            GlobalScope.launch (Dispatchers.Main){
                uploadRoom()
            }

        }

        binding.picCard.setOnClickListener{
           showImageSourceOptions()
            count++
        }

        binding.updateRoompic.setOnClickListener {
            if(count!=0){
                pickImages.launch("image/*")
            }else{
                Toast.makeText(this, " firstly select the profile photo for your room", Toast.LENGTH_SHORT).show()
            }


        }
        storageRef = FirebaseStorage.getInstance()


    }
    //@SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showDeleteRoomConfirmationDialog() // Call the confirmation dialog when the back button is pressed
    }

    private fun showDeleteRoomConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to discard the room?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                GlobalScope.launch (IO){
                    userkey?.let { deleteRoom("Rooms", it) }
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    suspend fun deleteRoom(collection1:String,collection2: String){
        val document = intent.getStringExtra("documentId")
        val docRef = document?.let { db.collection(collection1).document(it) }
        val docRef2 = document?.let { db.collection(collection2).document(it) }
        if (docRef != null) {
            docRef.delete()
                .addOnSuccessListener {
                    // Document successfully deleted
                    Toast.makeText(this, "DocumentSnapshot successfully deleted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error deleting document: $e")
                }
        }
        if (docRef2 != null) {
            docRef2.delete()
                .addOnSuccessListener {
                    // Document successfully deleted
                    val intent = Intent(this,owner_home_activity::class.java)
                    startActivity(intent)
                    finish()
                    println("DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error deleting document: $e")
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
            if (uri != null) {
                dpuri = uri
            }
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
        dpuri = uri
        binding.pic.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
                binding.progressBar.visibility = View.VISIBLE
                uplaodimage(userkey)
            }
            //
        }
    }
    private fun handleImageUri(uri: Uri) {
        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        binding.pic.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
                binding.progressBar.visibility = View.VISIBLE
                uplaodimage(userkey)
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



    private fun uplaodimage(userkey: String?) {
        if (dpuri == null) {
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show()
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val imageUrl = uploadImage1(dpuri)
                    saveDpUriToFirestore(imageUrl, userkey)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("Disha2",e.toString())
                        Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun uploadImage1(dpUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(dpUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()

    private suspend fun saveDpUriToFirestore(imageUrl: Uri, userkey: String?) {
        val roomId = userkey + "${num - 1}"
        try {
            val roomsCollection = db.collection("Rooms")
            val ownercollection = userkey?.let { db.collection(it) }

            roomsCollection.document(roomId).update("dpuri", imageUrl).await()
            ownercollection?.document(roomId)?.update("dpuri", imageUrl)?.await()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@add_room, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("Disha3",e.toString())
                Toast.makeText(this@add_room, "Error updating image in Firestore: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }



}