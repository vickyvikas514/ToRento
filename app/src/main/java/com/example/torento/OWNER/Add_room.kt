package com.example.torento.OWNER

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
import androidx.core.net.toUri
import androidx.transition.Visibility
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
//TODO room must saved to draft first, make draft section in the app.
//TODO showing dialog to wait unitil array is fillin in uploadImagesToFirebaseStorage
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
    private var dpUri : String = ""
    private var roomId:String=""
    val SHARED_PREF : String = "sharedPrefs"
    private var userkey:String? = ""
    private lateinit var dpuri:Uri
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private val imagesList = mutableListOf<Uri>()
    private val imagesListforFirebaseUris = mutableListOf<Uri>()
    private lateinit var auth: FirebaseAuth
    private lateinit var roomOwnerDpUrl:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storageRef = FirebaseStorage.getInstance()
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username", "")
        GlobalScope.launch(Dispatchers.IO) {
            GlobalScope.launch(Dispatchers.Main){
                Toast.makeText(this@add_room, "11", Toast.LENGTH_SHORT).show()
            }
            roomOwnerDpUrl = getOwnerDp()
        }
        val pickImages =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage()
                }}

        binding.uploadbtn.setOnClickListener {
            //Toast.makeText(this, "HELLO", Toast.LENGTH_SHORT).show()

            GlobalScope.launch (Dispatchers.Main){
                UploadTheRoom()
            }

        }
        binding.dpupdate.visibility = View.GONE
        binding.picCard.setOnClickListener{
           showImageSourceOptions()
        }
        binding.AddMorePics.setOnClickListener {
            if (this@add_room::dpuri.isInitialized && dpuri != "".toUri()){
                pickImages.launch("image/*")
            }else{
                Toast.makeText(this, "Please Select an image first for your room", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private suspend fun getOwnerDp():String = GlobalScope.async{
        GlobalScope.launch(Dispatchers.Main){
            Toast.makeText(this@add_room, "12", Toast.LENGTH_SHORT).show()
        }
        val docRef = db.collection("users").document(userkey.toString()).get().await()
        var Url = ""
        try {
            docRef.data?.let {
                Url = it["imageuri"].toString()
            }
        } catch(e:Exception) {
            Log.e("descripn", "Error fetching data from Firestore: ${e.message}")
        }
        GlobalScope.launch(Dispatchers.Main){
            Toast.makeText(this@add_room, Url, Toast.LENGTH_SHORT).show()
        }

        return@async Url
    }.await()
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
                    val intent = Intent(this@add_room, owner_home_activity::class.java)
                    startActivity(intent)
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
        binding.pic.setImageURI(uri)
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
        binding.pic.setImageURI(uri)
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
    private suspend fun uploadBG(uri: Uri?){
        storageRef = FirebaseStorage.getInstance()
        return try{
            val reference = storageRef.getReference("images").child(System.currentTimeMillis().toString())
            reference.putFile(uri!!).await() // Await the completion of the upload
            val downloadUrl = reference.downloadUrl.await()
            dpUri = downloadUrl.toString()// Await download URL
        } catch(e:Exception){
            Log.d("ExceptionInAddingMoreImages",e.toString())
            Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
        }

    }
    private fun uploadImagesToFirebaseStorage() {
        showProgressOverlay(true)
        GlobalScope.launch(Dispatchers.IO) {
            for ((index, imageUri) in imagesList.withIndex()) {
                try {
                    val imageUrl = uploadImage(imageUri)
                    imagesListforFirebaseUris.add(imageUrl)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("ExceptionInAddingMoreImages",e.toString())
                        Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                showProgressOverlay(false)
                Toast.makeText(this@add_room, "All images uploaded successfully", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private suspend fun uploadImage(imageUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()
    private fun showProgressOverlay(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isClickable = show
        binding.root.isFocusable = show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    private fun UploadTheRoom(){
        showProgressOverlay(true)
        auth = FirebaseAuth.getInstance()
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        location = binding.Locality.text.toString()
        loaction_description = binding.locationDescription.text.toString()
        amount = binding.amount.text.toString()
        owner_name = binding.OwnerName.text.toString()
        breif_description = binding.RoomDescription.text.toString()
        if (isInputDataValid()) {
            CoroutineScope(Dispatchers.Main).launch {
                userkey?.let { key ->
                    if (this@add_room::dpuri.isInitialized && dpuri != "".toUri()) {
                        binding.progressBar.visibility = View.VISIBLE
                        // Wait for the upload to complete
                        uploadBG(dpuri)
                    }
                }
                if(dpUri.isNotEmpty()){
                    val updateData = hashMapOf(
                        "length" to length,
                        "width" to width,
                        "location" to location,
                        "location_detail" to loaction_description,
                        "owner_name" to owner_name,
                        "amount" to amount,
                        "breif_description" to breif_description,
                        "roomId" to roomId,
                        "dpuri" to dpUri,
                        "imageuri" to imagesListforFirebaseUris,
                        "ownerId" to auth.currentUser?.uid.toString(),
                        "ownerDpUrl" to roomOwnerDpUrl,

                    )
                    saveRoomData(updateData)
                }else{
                    Toast.makeText(this@add_room, "Please Select an image first for your room", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            showProgressOverlay(false)
            Toast.makeText(this, "Please provide all the details", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isInputDataValid(): Boolean {
        // Validate all the necessary input fields here
        return length.isNotEmpty() && width.isNotEmpty() && location.isNotEmpty() &&
                loaction_description.isNotEmpty() && amount.isNotEmpty() &&
                owner_name.isNotEmpty() && breif_description.isNotEmpty()
    }
    private fun saveRoomData(updateData: HashMap<String, Any>) {
        val docref2 = db.collection(userkey.toString())
        if (docref2 != null) {
            docref2.add(updateData)
                .addOnSuccessListener {
                    val documentId = it.id
                    roomId = documentId
                    val docRefUser = db.collection("Rooms").document(documentId)
                    if (docRefUser != null) {

                        updateData["roomId"] = documentId
                        docref2.document(documentId).update(updateData)
                            .addOnSuccessListener {
                                docRefUser.set(updateData)
                                    .addOnSuccessListener {
                                        showProgressOverlay(false)
                                        Toast.makeText(
                                            this@add_room, "Success", Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        backtoHome()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this@add_room,
                                            "failure",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }

                    }
                    Toast.makeText(this@add_room, "Success", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@add_room, "failure", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
    private fun backtoHome(){
        val intent = Intent(
            this@add_room, owner_home_activity::class.java
        )
        startActivity(intent)
        finish()
    }
    override fun onStop() {
        super.onStop()
        //room must get to draft
        // If user exits without saving, delete the temporary room
        GlobalScope.launch(Dispatchers.IO) {
            if (!checkIfAllDetailsFilled()) {
                //userkey?.let { deleteRoom("Rooms", it) }
            }
        }
    }
    private suspend fun checkIfAllDetailsFilled(): Boolean {
        val length = binding.roomlength.text.toString()
        val width = binding.roomwidth.text.toString()
        val location = binding.Locality.text.toString()
        val loaction_description = binding.locationDescription.text.toString()
        val amount = binding.amount.text.toString()
        val owner_name = binding.OwnerName.text.toString()
        val breif_description = binding.RoomDescription.text.toString()
        return !(length.isEmpty() || width.isEmpty() || location.isEmpty() || loaction_description.isEmpty() || amount.isEmpty() || owner_name.isEmpty() || breif_description.isEmpty())
    }
}