package com.example.torento.OWNER

import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.torento.LOGIN.LandingPage.Companion.num
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

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
    val SHARED_PREF : String = "sharedPrefs"
    private lateinit var dpuri:Uri
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private val imagesList = mutableListOf<Uri>()


    private fun uploadImagesToFirebaseStorage(userkey: String?) {

        for ((index, imageUri) in imagesList.withIndex()) {

                val imageName = "image_$index.jpg"
                val imageRef = storageref.child(imageName)

                // Upload image to Firebase Storage
                imageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl
                            ?.addOnSuccessListener { imageuri ->
                                Toast.makeText(this, "part-a", Toast.LENGTH_SHORT).show()
                                saveImageUrlToFirestore(imageuri,userkey)
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(this, "part-af", Toast.LENGTH_SHORT).show()   //handle error
                            }



                    }
                    .addOnFailureListener {
                        // Handle the error
                    }

        }//for loop

    }

    private fun saveImageUrlToFirestore(imageUrl: Uri, userkey: String?) {
        val roomsCollection = db.collection("Rooms")
        val ownercollection = userkey?.let { db.collection(it) }
        // Replace 'roomId' with the actual ID of the room document
        val roomId = userkey+"${num-1}"
        Log.d("chutiya",roomId)
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        location = binding.Locality.text.toString()
        loaction_description = binding.locationDescription.text.toString()
        amount = binding.amount.text.toString()
        owner_name = binding.OwnerName.text.toString()
        breif_description = binding.RoomDescription.text.toString()
        if(length.isNotEmpty()&&width.isNotEmpty()&&dpuri.toString().isNotEmpty()){
            val room = hashMapOf(
                "length" to length,
                "width" to width,
                "location" to location,
                "location_detail" to loaction_description,
                "owner_name" to owner_name,
                "amount" to amount,
                "breif_description" to breif_description,
                "imageuri" to FieldValue.arrayUnion(imageUrl)

            )
            if(roomId!="temp"){
                roomsCollection.document(roomId).update(room as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "error in updating", Toast.LENGTH_SHORT).show()
                    }
                if (ownercollection != null) {
                    ownercollection.document(roomId).update(room as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "error in updating", Toast.LENGTH_SHORT).show()
                        }
                }
            }else{
                Toast.makeText(this, "userkey is equal to temp", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "provide all the details", Toast.LENGTH_SHORT).show()
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
        val userkey: String? = sharedPreferences.getString("username", "")
        ///////////////////////
        val pickImages =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage(userkey)
                }
            }
        val galleryimage =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                binding.pic.setImageURI(it)
                if (it != null) {
                    dpuri = it
                }
                uplaodimage(userkey)
            }
        ////////////////////////

        ////////////////////////

        binding.picCard.setOnClickListener{
            galleryimage.launch("image/*")
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

    private fun uplaodimage(userkey: String?) {
        if(dpuri==null){
            Toast.makeText(this, "select image", Toast.LENGTH_SHORT).show()
        }else{
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(dpuri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Part-1", Toast.LENGTH_SHORT).show()
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { imageuri ->
                            Toast.makeText(this, "Part-1a", Toast.LENGTH_SHORT).show()
                            val roomsCollection = db.collection("Rooms")
                            val ownercollection = userkey?.let { it1 -> db.collection(it1) }
                            val roomId = userkey+"${num-1}"
                            Log.d("terabaap1","${num-1}")
                            Log.d("vicky4","$roomId")
                            roomsCollection.document(roomId).update("dpuri" , imageuri)
                            if (ownercollection != null) {
                                ownercollection.document(roomId).update("dpuri" , imageuri)
                            }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Part-3", Toast.LENGTH_SHORT).show()
                } } }



}