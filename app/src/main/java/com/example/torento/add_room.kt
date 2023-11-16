package com.example.torento

import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlin.properties.Delegates

class add_room : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var db = Firebase.firestore
    private var storageRef = Firebase.storage
    private lateinit var length:String
    private lateinit var width:String
    private lateinit var location:String
    var x=0
    val SHARED_PREF: String = "sharedPrefs"
    private lateinit var dpuri:Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storageRef = FirebaseStorage.getInstance()
        binding.upload.setOnClickListener{
            uplaodimage()
        }
        val galleryimage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.pic.setImageURI(it)
                if (it != null) {
                    dpuri = it
                }
            }
        )
        binding.updateRoompic.setOnClickListener {
           galleryimage.launch("image/*")

        }
        storageRef = FirebaseStorage.getInstance()
        binding.upload.setOnClickListener{
            uplaodimage()
        }

    }
    private fun uplaodimage(){
        if(dpuri==null){
            Toast.makeText(this, "select image", Toast.LENGTH_SHORT).show()
        }else{

            val collectionReference = db.collection("Rooms")
            collectionReference.get()
                .addOnSuccessListener { querySnapshot ->
                    // Get the count of documents in the collection
                    var itemCount = querySnapshot.size()
                    x = itemCount+1
                  }
                .addOnFailureListener { e ->
                    x=1
                    Toast.makeText(this, "fail in room count", Toast.LENGTH_SHORT).show()
                }
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(dpuri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Part-1", Toast.LENGTH_SHORT).show()
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { imageuri ->
                            Toast.makeText(this, "Part-2", Toast.LENGTH_SHORT).show()
                            length = binding.roomlength.text.toString()
                            width = binding.roomwidth.text.toString()
                            location = binding.locationDetail.text.toString()
                            if(length.isNotEmpty()&&width.isNotEmpty()&&dpuri.toString().isNotEmpty()){
                                val room = hashMapOf(
                                    "length" to length,
                                    "width" to width,
                                    "location" to location,
                                    "imageuri" to imageuri
                                )
                                val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
                                val userkey: String? = sharedPreferences.getString("username", "")
                                if (userkey != null) {
                                    if (userkey.isNotEmpty()){

                                        db.collection("Rooms").document(userkey+"$x")
                                            .set(room)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Room is set", Toast.LENGTH_SHORT).show()
                                                Log.d("vikas", "Success $userkey$x")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("vikas", "Error adding document", e)
                                            }

                                        db.collection(userkey).document(userkey+"$x")
                                            .set(room)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Room is set", Toast.LENGTH_SHORT).show()
                                                Log.d("vikas", "Success $userkey$x")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("vikas", "Error adding document", e)
                                            }
                                    }else{
                                        Toast.makeText(this, "userkey is empty", Toast.LENGTH_SHORT).show()
                                    }

                                }else{
                                    Toast.makeText(this, "userkey is null", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this, "please fill all the details while selecting the image for the room", Toast.LENGTH_SHORT).show()
                            }


                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Part-3", Toast.LENGTH_SHORT).show()
                }
        }


    }


    private fun UpdateRoom(){
        if(x!=0){
             val collectionReference = db.collection("Rooms")
            collectionReference.get()
                .addOnSuccessListener { querySnapshot ->
                    // Get the count of documents in the collection
                    var itemCount = querySnapshot.size()
                    x = itemCount+1
                    //numberofitem = itemCount.toString()
                    // Use itemCount as needed

                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(this, "fail in room count", Toast.LENGTH_SHORT).show()
                }
        }

        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        if(length.isNotEmpty()&&width.isNotEmpty()&&dpuri.toString().isNotEmpty()) {
            val room1 = hashMapOf(
                "length" to length,
                "width" to width,
                "imageuri" to dpuri
            )
            val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
            val userkey: String? = sharedPreferences.getString("username", "")
            if (userkey != null) {

                db.collection("Rooms").document(userkey+"$x")
                    .set(room1)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Room is set", Toast.LENGTH_SHORT).show()
                        Log.d("vikas", "Success $userkey$x")
                    }
                    .addOnFailureListener { e ->
                        Log.w("vikas", "Error adding document", e)
                    }
                db.collection(userkey).document("items"+"$x")
                    .set(room1)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User is set", Toast.LENGTH_SHORT).show()
                        Log.d("vikas", "Success $userkey$x")
                    }
                    .addOnFailureListener { e ->
                        Log.w("vikas", "Error adding document", e)
                    }
            }


        }else{
            Toast.makeText(this, "please fill all the details while selecting the image for the room", Toast.LENGTH_SHORT).show()
        }

    }
}