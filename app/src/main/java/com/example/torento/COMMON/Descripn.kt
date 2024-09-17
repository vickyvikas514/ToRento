package com.example.torento.COMMON

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torento.Adapter.PicsAdapter
import com.example.torento.LOGIN.LandingPage.Companion.usertype
import com.example.torento.OWNER.EditRoom
import com.example.torento.OWNER.owner_home_activity

import com.example.torento.R
import com.example.torento.USER.user_home_activity
import com.example.torento.databinding.ActivityDescripnBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext



// TODO discripn mai tho main photo hai hi nhi that is DP which shows in home activity

class descripn : AppCompatActivity() {
    private lateinit var binding: ActivityDescripnBinding
    private var db = Firebase.firestore
    val SHARED_PREF: String = "sharedPrefs"
    private lateinit var heartButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usertype: String
    private lateinit var username: String
    private lateinit var ownerId:String
    private lateinit var RoomData:Map<String,Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescripnBinding.inflate(layoutInflater)
        setContentView(binding.root)

       val documentid = intent.getStringExtra("documentid")
        usertype = intent.getStringExtra("usertype").toString()
        username = intent.getStringExtra("username").toString()
        ownerId = intent.getStringExtra("ownerId").toString()
//        Toast.makeText(this,intent.getStringExtra("username").toString(), Toast.LENGTH_SHORT).show()
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        GlobalScope.launch(Dispatchers.IO) {retreivingdataBG()}
        if(usertype=="owner"){
            binding.saveBtn.text = "Change Room details"
            binding.saveBtn.setOnClickListener {
                val intent = Intent(this@descripn,EditRoom::class.java)

                intent.putExtra("documentid",documentid)
                intent.putExtra("ownerId",ownerId)
                startActivity(intent)
            }
            binding.delete.setOnClickListener {
                GlobalScope.launch (IO){
                    intent.getStringExtra("ownerId")
                        ?.let { it1 -> deleteRoom("Rooms", it1,
                            intent.getStringExtra("documentid")!!
                        ) }

                }
            }
            binding.heartButton.visibility = View.GONE
        }else{
            binding.saveBtn.visibility = View.GONE

            // Adjust the layout to remove space occupied by the Save button
            val params = binding.saveBtn.layoutParams as ConstraintLayout.LayoutParams
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            heartButton = findViewById(R.id.heartButton)

            // Check if room is liked and update the heart button
            updateHeartButtonState(isRoomLiked())


                // Set click listener for the heart button
                heartButton.setOnClickListener {
                    // Toggle the like status
                    val isLiked = toggleLikeStatus()
                    // Update the heart button state
                    updateHeartButtonState(isLiked)
                    if(isLiked){
                        GlobalScope.launch(Dispatchers.IO) {
                            if (RoomData != null) {
                                save(RoomData,documentid.toString())
                            }
                        }
                    }else
                    {
                        GlobalScope.launch (Dispatchers.IO){
                            unsave(username,documentid.toString())
                        }
                    }
                   }
            binding.delete.visibility = View.INVISIBLE
        }
    //fetchDataFromFirestore

        binding.chatBtn.setOnClickListener {
            if(usertype=="owner"){
                changetoChatbyOwner(ownerId,documentid)//userId0 ko ownerId kiya hai
            }else{
              changetoChat(intent.getStringExtra("userId"),documentid,username,usertype)
            }
        }
        binding.listPhoto.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        supportActionBar?.setTitle("KAMRE")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.brown)))

    }
    private fun isRoomLiked(): Boolean {
        // Retrieve the like status from SharedPreferences
        return sharedPreferences.getBoolean("isLiked", false)
    }
    private fun toggleLikeStatus(): Boolean {
        // Toggle the like status and save it to SharedPreferences
        val isLiked = !isRoomLiked()
        sharedPreferences.edit().putBoolean("isLiked", isLiked).apply()
        return isLiked
    }
    private fun updateHeartButtonState(isLiked: Boolean) {
        // Update the heart button drawable based on the like status
        val heartDrawable = if (isLiked) R.drawable.empty_heart else R.drawable.black_heart
        heartButton.setImageResource(heartDrawable)
    }
    private fun changetoChat(userId: String?, documentid: String?, username: String?,usertype:String) {
        val intent = Intent(this@descripn, ChatActivity::class.java)
        intent.putExtra("userId",ownerId)
        intent.putExtra("documentid",documentid)
        intent.putExtra("username",username)
        intent.putExtra("usertype",usertype)
        Toast.makeText(this,documentid, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    private fun changetoChatbyOwner(userId: String?, documentid: String?) {
        val intent = Intent(this@descripn, ChatListActivity::class.java)
        intent.putExtra("documentid",documentid)
        Toast.makeText(this, userId, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = if(usertype=="owner") Intent(this@descripn, owner_home_activity::class.java) else Intent(this@descripn, user_home_activity::class.java)
        startActivity(intent)
        finish()
    }
    suspend fun retreivingdataBG() {
        val Id = intent.getStringExtra("documentid").toString()
       var imageUriList:List<String> = listOf<String>()
       var roomData: MutableMap<String, Any>? = null
//        GlobalScope.launch (Dispatchers.Main){
////            Toast.makeText(this@descripn, username+"1234", Toast.LENGTH_SHORT).show()
////            Toast.makeText(this@descripn, Id+"456", Toast.LENGTH_SHORT).show()
//        }

           val docref =  db.collection(ownerId).document(Id)
               docref.get()
               .addOnSuccessListener { documentSnapshot ->
                   if (documentSnapshot.exists()) {
                        roomData = documentSnapshot.data
//                       list.add(roomData?.get("location_detail")?.toString() ?: "Default Location")
//                       list.add(roomData?.get("amount")?.toString() ?: "0")
//                       list.add(roomData?.get("owner_name")?.toString() ?: "Unknown Owner")
//                       list.add(roomData?.get("breif_description")?.toString() ?: "No Description")
                        RoomData = roomData!!
                       imageUriList = roomData?.get("imageuri") as? List<String> ?: listOf()
                       if (imageUriList.isEmpty()) {
                           val packageName = applicationContext.packageName
                           imageUriList = listOf("android.resource://$packageName/drawable/demodp")
                       }


                       val addressrecieved = roomData?.get("address") as? Map<String, Any>
                       val orderedKeys = listOf("house_no", "locality", "district", "state", "pincode")
                       val addressString = orderedKeys.joinToString(separator = ", ") { key ->
                           addressrecieved?.get(key)?.toString() ?: "" // Use empty string if key doesn't exist
                       }
                       binding.fullLocanDetsil.text = addressString
                       binding.amount.text = roomData?.get("amount") as? String ?: "Nil"
                       binding.ownerName.text = roomData?.get("owner_name") as? String ?: "No owner name is found"
                       binding.breifDescription.text = roomData?.get("breif_description") as? String ?: "Set a description"
                       // Set the new adapter to the RecyclerView
                       binding.listPhoto.adapter = PicsAdapter(this,imageUriList)

                   } else {
                       Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                   }
               }
               .addOnFailureListener { e ->
                   Toast.makeText(this, "Failed to fetch data: $e", Toast.LENGTH_SHORT).show()
               }

          /* if (docref != null) {
               //Toast.makeText(this@descripn, "docref is not null", Toast.LENGTH_SHORT).show()
               dataMap = docref.data

                   list.add(dataMap?.get("location_detail")?.toString() ?: "Default Location")
                   list.add(dataMap?.get("amount")?.toString() ?: "0")
                   list.add(dataMap?.get("owner_name")?.toString() ?: "Unknown Owner")
                   list.add(dataMap?.get("breif_description")?.toString() ?: "No Description")
                   imageUriList = dataMap?.get("imageuri") as? List<String> ?: listOf()

               Log.d("chaudhary1", list.size.toString())
           } else {
               Toast.makeText(this, "DocRef is NULL", Toast.LENGTH_SHORT).show()
           }*/
           /*if (imageUriList.isEmpty()) {
               val packageName = applicationContext.packageName
               imageUriList = listOf("android.resource://$packageName/drawable/demodp")
           }*/
      return

    }
    suspend fun save(data: Map<String, Any>,Id:String,) {
        // retrieve the user key (you may have this logic somewhere)
            if (username != null) {
                try {
                    // Store room information in the user's collection
                    val userRoomRef = db.collection(username).document(Id)
                    userRoomRef.set(data)
                        .addOnSuccessListener {
                            Toast.makeText(this@descripn, "SAVE", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("descripn", "Error saving room information: $e")
                        }
                } catch (e: Exception) {
                    Log.e("descripn", "Error saving room information: ${e.message}")
                }
            }
    }
    suspend fun deleteRoom(collection1:String,collection2: String,document:String){

    val docRef = db.collection(collection1).document(document)
    val docRef2 = db.collection(collection2).document(document)
    docRef.delete()
        .addOnSuccessListener {
            // Document successfully deleted
            docRef2.delete()
                .addOnSuccessListener {
                    // Document successfully deleted
                    println("DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error deleting document: $e")
                }
            val intent = Intent(this@descripn, owner_home_activity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "DocumentSnapshot successfully deleted!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            // Handle the error
            println("Error deleting document: $e")
        }

}
    suspend fun unsave(collection1:String,document:String){

        val docRef = db.collection(collection1).document(document)

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

}