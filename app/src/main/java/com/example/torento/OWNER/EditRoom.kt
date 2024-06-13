package com.example.torento.OWNER

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class EditRoom : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var storageRef = Firebase.storage
    val SHARED_PREF: String = "sharedPrefs"
    private var db = com.google.firebase.ktx.Firebase.firestore
    private lateinit var Id:String
    private val imagesList = mutableListOf<Uri>()
    private val imagesListforFirebaseUris = mutableListOf<Uri>()
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private lateinit var userkey: String
    private var check:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addroomtext.text = "Edit your room details"
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username", "").toString()

        Id = intent.getStringExtra("documentid").toString()
        Toast.makeText(this@EditRoom, Id, Toast.LENGTH_SHORT).show()
        binding.uploadbtn.text = "Save the changes"
        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.pic.setImageURI(it)
            if (it != null) {
                val dpUri = it
                GlobalScope.launch(Dispatchers.IO) {
                    val uploadedImageUri = async { changeDPBG(dpUri) }.await()
                    if (userkey != null) {
                        savechanges(uploadedImageUri,Id,userkey).await()
                        withContext(Dispatchers.Main){
                            binding.progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        binding.dpupdate.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            galleryImage.launch("image/*") }
        binding.uploadbtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (userkey != null) {
                updateTheEdit(Id,userkey)
            }else{
                Toast.makeText(this, "username is not found", Toast.LENGTH_SHORT).show()
            }
            changetohome()
        }
        GlobalScope.launch (Dispatchers.IO){
            retreivingdataBG(Id,userkey.toString())
        }
        val pickImages =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage()
                }}
        binding.AddMorePics.setOnClickListener{
            val options = arrayOf("Remove all the previous images and add new images", "Add some new images to the existing images")

            // Create a dialog for options
            AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            // Choose from Gallery option selected
                            pickImages.launch("image/*")
                        }
                        1 -> {
                            // Take Photo option selected
                            check = 1
                            pickImages.launch("image/*")
                            AddSomeNewImages()
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    /*private fun ShowDialogForAddmorePics(){
        // Define options in an array
        val options = arrayOf("Remove all the previous images and add new images", "Add some new images to the existing images")

        // Create a dialog for options
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Choose from Gallery option selected
                        RemoveAllImages()
                    }
                    1 -> {
                        // Take Photo option selected
                        AddSomeNewImages()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }*/
    private fun RemoveAllImages(){

        GlobalScope.launch (Dispatchers.IO){
            AddAlltheNewImages()
        }
    }
    private suspend fun AddAlltheNewImages() = GlobalScope.async{


    }.await()
    private fun AddSomeNewImages(){

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
                        Toast.makeText(this@EditRoom, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                showProgressOverlay(false)
                Toast.makeText(this@EditRoom, "All images uploaded successfully", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun showProgressOverlay(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isClickable = show
        binding.root.isFocusable = show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    private suspend fun uploadImage(imageUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()
    private fun updateTheEdit(Id: String,userkey: String){
        val length:String? = binding.roomlength.text.toString()
       val width:String? = binding.roomwidth.text.toString()
        val location:String? = binding.Locality.text.toString()
        val loaction_description:String? = binding.locationDescription.text.toString()
        val amount:String? = binding.amount.text.toString()
        val owner_name:String? = binding.OwnerName.text.toString()
        val breif_description:String? = binding.RoomDescription.text.toString()

        GlobalScope.launch (Dispatchers.IO){

            val deferred1 = async {
                    Log.d("CHECKJIJI",length.toString())
                        changeMeasurements(length, width, Id, userkey)
            }
            val deferred2 = async {

                    changeD(Id, breif_description, userkey)

            }
            val deferred3 = async {

                    changeL(Id, location, userkey)

            }
            val deferred4 = async {

                    changeLD(Id, loaction_description, userkey)

            }
            val deferred5 = async {

                    changeON(owner_name, Id, userkey)

            }
            val deferred6 = async {

                    changePrice(Id, amount, userkey)

            }
            val deferred7 = async {
            if(check==1){
                appendImagesToFirestore()
            }
            else {
                changeMorePics()
            }
            }
            deferred1.await()
            deferred2.await()
            deferred3.await()
            deferred4.await()
            deferred5.await()
            deferred6.await()
            deferred7.await()

            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@EditRoom, "your Room has been updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun changeMeasurements(length:String?,width:String?,Id: String,userkey: String){
        length?.let { db.collection("Rooms").document(Id).update("length",length) }
        length?.let{ db.collection(userkey).document(Id).update("length", length) }
        width?.let{ db.collection("Rooms").document(Id).update("width", width) }
        width?.let{ db.collection(userkey).document(Id).update("width", width) }
    }
    suspend fun changeON(ON:String?,Id: String,userkey:String){
        ON?.let{
            db.collection(userkey).document(Id).update("owner_name", ON)
            db.collection("Rooms").document(Id).update("owner_name", ON)
        }
    }

    suspend fun retreivingdataBG(Id:String,userKey: String){
        if (userKey != null) {
            val roomRef = db.collection("Rooms").document(Id)
            roomRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val roomData = documentSnapshot.data
                        // Update EditText fields with existing data
                        GlobalScope.launch (Dispatchers.Main){
                            updateEditTextFields(roomData)
                        }

                    } else {
                        Toast.makeText(this@EditRoom, "Document not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@EditRoom, "Failed to fetch data: $e", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun updateEditTextFields(roomData: Map<String, Any>?) {
        if (roomData != null) {
            // Example: Update length EditText field
            val length = roomData["length"] as? String
            binding.roomlength.setText(length)
            binding.roomwidth.setText(roomData["width"] as? String)
            binding.amount.setText(roomData["amount"] as? String)
            binding.Locality.setText(roomData["location"] as? String)
            binding.locationDescription.setText(roomData["location_detail"] as? String)
            binding.OwnerName.setText(roomData["owner_name"] as? String)
            binding.RoomDescription.setText(roomData["breif_description"] as? String)
            //Toast.makeText(this@EditRoom, roomData["imageuri"] as? String, Toast.LENGTH_SHORT).show()
            Glide.with(this)
                .load(roomData["dpuri"] as? String)
                .into(binding.pic)
        }
    }

    suspend fun changeL(Id: String,location:String?,userkey:String){
        location?.let{
            db.collection(userkey).document(Id).update("location", location)
            db.collection("Rooms").document(Id).update("location", location)
        }
    }
    suspend fun changeMorePics(){
        imagesListforFirebaseUris?.let{
            db.collection(userkey).document(Id).update("imageuri", imagesListforFirebaseUris)
            db.collection("Rooms").document(Id).update("imageuri", imagesListforFirebaseUris)
        }
    }
    suspend fun changeLD(Id: String,LD:String?,userkey:String){
       LD?.let {
            db.collection("Rooms").document(Id).update("location_detail", LD)
            db.collection(userkey).document(Id).update("location_detail", LD)
        }
    }
    suspend fun changePrice(Id: String,price:String?,userkey:String){
        price?.let{
            db.collection("Rooms").document(Id).update("amount", price)
            db.collection(userkey).document(Id).update("amount", price)
        }
    }
    suspend fun changeD(Id: String,descrpn:String?,userkey:String){
        descrpn?.let{
            db.collection("Rooms").document(Id).update("breif_description", descrpn)
            db.collection(userkey).document(Id).update("breif_description", descrpn)
        }
    }
    suspend fun savechanges(uploadedImageUri:Uri,Id:String,userkey:String): Deferred<Unit> = GlobalScope.async{
       db.collection("Rooms").document(Id).update("dpuri" , uploadedImageUri)
        db.collection(userkey).document(Id).update("dpuri" , uploadedImageUri)

    }
    suspend fun changeDPBG(dpUri:Uri) : Uri{
        if (dpUri != null) {
            try {
                val imageRef = storageRef.getReference("Dpimages").child(System.currentTimeMillis().toString())
                val uploadTask = imageRef.putFile(dpUri)

                val imageUrl = Tasks.await(uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                })

                return imageUrl
            } catch (e: Exception) {
                Log.e("ErrorinER", "Failed to change DP image: $e")
            }
        } else {
            Log.e("ErrorinER", "dpUri is NULL")
        }

        return Uri.EMPTY
    }
    override fun onBackPressed() {
        showDeleteRoomConfirmationDialog() // Call the confirmation dialog when the back button is pressed
    }
    private fun showDeleteRoomConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Your room deatils are not saved yet. Are you sure you want to delete this room?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                GlobalScope.launch (IO){
                    changetohome()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun changetohome(){
        val intent = Intent(this@EditRoom, owner_home_activity::class.java)
        startActivity(intent)
        finish()
    }
    private suspend fun appendImagesToFirestore() {
        val roomRef = db.collection("Rooms").document(Id)
        val userRef = db.collection(userkey).document(Id)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                val existingImages = snapshot.get("imageuri") as? MutableList<String> ?: mutableListOf()

                imagesListforFirebaseUris.forEach {
                    existingImages.add(it.toString())
                }

                transaction.update(roomRef, "imageuri", existingImages)
                transaction.update(userRef, "imageuri", existingImages)
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreError", "Failed to append images: $e")
        }
    }
}