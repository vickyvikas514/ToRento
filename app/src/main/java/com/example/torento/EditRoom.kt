package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditRoom : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var storageRef = Firebase.storage
    val SHARED_PREF: String = "sharedPrefs"
    private var db = com.google.firebase.ktx.Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        Toast.makeText(this@EditRoom, userkey, Toast.LENGTH_SHORT).show()
        val Id = intent.getStringExtra("documentid").toString()
        Toast.makeText(this@EditRoom, Id, Toast.LENGTH_SHORT).show()
        binding.updateRoompic.text = "Save the changes"
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

        binding.picCard.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            galleryImage.launch("image/*") }
        binding.updateRoompic.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (userkey != null) {
                updateTheEdit(Id,userkey)
            }

        }
    }
    private fun updateTheEdit(Id: String,userkey: String){
        val length = binding.roomlength.text.toString()
       val width = binding.roomwidth.text.toString()
        val location = binding.Locality.text.toString()
        val loaction_description = binding.locationDescription.text.toString()
        val amount = binding.amount.text.toString()
        val owner_name = binding.OwnerName.text.toString()
        val breif_description = binding.RoomDescription.text.toString()
        GlobalScope.launch (Dispatchers.IO){
            val deferred1 = async { changeMeasurements(length, width, Id, userkey) }
            val deferred2 = async { changeD(Id, breif_description, userkey) }
            val deferred3 = async { changeL(Id, location, userkey) }
            val deferred4 = async { changeLD(Id, loaction_description, userkey) }
            val deferred5 = async { changeON(owner_name, Id, userkey) }
            val deferred6 = async { changePrice(Id, amount, userkey) }
            deferred1.await()
            deferred2.await()
            deferred3.await()
            deferred4.await()
            deferred5.await()
            deferred6.await()

            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@EditRoom, "your Room has been updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun changeMeasurements(length:String,width:String,Id: String,userkey: String){
       db.collection("Rooms").document(Id).update("length",length)
       db.collection(userkey).document(Id).update("length",length)
       db.collection("Rooms").document(Id).update("width" , width)
       db.collection(userkey).document(Id).update("width" , width)
    }
    suspend fun changeON(ON:String,Id: String,userkey:String){
       db.collection(userkey).document(Id).update("owner_name" , ON)
       db.collection("Rooms").document(Id).update("owner_name" , ON)
    }
    suspend fun changeL(Id: String,location:String,userkey:String){
       db.collection(userkey).document(Id).update("location" , location)
       db.collection("Rooms").document(Id).update("location" , location)
    }
    suspend fun changeLD(Id: String,LD:String,userkey:String){
       db.collection("Rooms").document(Id).update("location_detail" , LD)
       db.collection(userkey).document(Id).update("location_detail" , LD)
    }
    suspend fun changePrice(Id: String,price:String,userkey:String){
       db.collection("Rooms").document(Id).update("amount" , price)
       db.collection(userkey).document(Id).update("amount" , price)
    }
    suspend fun changeD(Id: String,descrpn:String,userkey:String){
       db.collection("Rooms").document(Id).update("breif_description" , descrpn)
       db.collection(userkey).document(Id).update("breif_description" , descrpn)
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
    override fun onBackPressed(){
        super.onBackPressed()
        val intent = Intent(this,owner_home_activity::class.java)
        startActivity(intent)
        finish()

    }
}