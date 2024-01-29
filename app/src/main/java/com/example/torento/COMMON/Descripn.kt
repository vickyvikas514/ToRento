package com.example.torento.COMMON

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torento.Adapter.PicsAdapter
import com.example.torento.databinding.ActivityDescripnBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class descripn : AppCompatActivity() {
    private lateinit var binding: ActivityDescripnBinding
    private var db = Firebase.firestore
    val SHARED_PREF: String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescripnBinding.inflate(layoutInflater)
        setContentView(binding.root)

       val documentid = intent.getStringExtra("documentid")
        val usertype = intent.getStringExtra("usertype")
        val username = intent.getStringExtra("username")

        if(usertype=="owner"){
            binding.saveBtn.text = "Change Room details"
            binding.delete.setOnClickListener {
                GlobalScope.launch (IO){
                    intent.getStringExtra("collection2")
                        ?.let { it1 -> deleteRoom("Rooms", it1,
                            intent.getStringExtra("documentid")!!
                        ) }

                }
            }
        }else{
            binding.delete.visibility = View.INVISIBLE
        }
    //fetchDataFromFirestore
      set()
        binding.chatBtn.setOnClickListener {
            if(usertype=="owner"){
                changetoChatbyOwner(intent.getStringExtra("userIdO"),documentid)
            }else{
              changetoChat(intent.getStringExtra("userId"),documentid,username,usertype.toString())
            }
        }
        binding.listPhoto.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)


    }
    private fun changetoChat(userId: String?, documentid: String?, username: String?,usertype:String) {
        val intent = Intent(this@descripn, ChatActivity::class.java)
        intent.putExtra("userId",userId)
        intent.putExtra("documentid",documentid)
        intent.putExtra("username",username)
        intent.putExtra("usertype",usertype)
        Toast.makeText(this,documentid, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    private fun changetoChatbyOwner(userId: String?, documentid: String?) {
        val intent = Intent(this@descripn, ChatListActivity::class.java)
        intent.putExtra("userId",userId)
        intent.putExtra("documentid",documentid)
        Toast.makeText(this, userId, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    private fun set(){
        GlobalScope.launch(Dispatchers.IO) {
            val (list, imagelist) = async { retreivingdataBG() }.await()
            withContext(Dispatchers.Main){
               retreivingdata(list,imagelist)
            }
        }
    }

    private fun retreivingdata(list:MutableList<String>,imageUriList:List<String>){
        binding.fullLocanDetsil.text = list[0]
        binding.amount.text =list[1]
        binding.ownerName.text =list[2]
        binding.breifDescription.text =list[3]

        // Set the new adapter to the RecyclerView
        binding.listPhoto.adapter = PicsAdapter(this,imageUriList)
    }
   suspend fun retreivingdataBG() : Pair<MutableList<String>,List<String>>{
        val Id = intent.getStringExtra("documentid").toString()
        val list:MutableList<String> = mutableListOf<String>()
       var imageUriList:List<String> = listOf<String>()
       try {
           val docref =  db.collection("Rooms").document(Id).get().await()
           if (docref != null) {
               docref.data?.let {
                   list.add(it["location_detail"].toString())
                   list.add(it["amount"].toString())
                   list.add(it["owner_name"].toString())
                   list.add(it["breif_description"].toString())
                   imageUriList = it["imageuri"] as? List<String> ?: emptyList()
               }
               Log.d("chaudhary1", list.size.toString())
           }
           else { Toast.makeText(this, "DocRef is NULL", Toast.LENGTH_SHORT).show() }
       } catch (e:Exception){
           Log.e("descripn", "Error fetching data from Firestore: ${e.message}")
       }

       return Pair(list,imageUriList)
    }

suspend fun deleteRoom(collection1:String,collection2: String,document:String){

    val docRef = db.collection(collection1).document(document)
    val docRef2 = db.collection(collection2).document(document)
    docRef.delete()
        .addOnSuccessListener {
            // Document successfully deleted
            Toast.makeText(this, "DocumentSnapshot successfully deleted!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            // Handle the error
            println("Error deleting document: $e")
        }
    docRef2.delete()
        .addOnSuccessListener {
            // Document successfully deleted
            println("DocumentSnapshot successfully deleted!")
        }
        .addOnFailureListener { e ->
            // Handle the error
            println("Error deleting document: $e")
        }
}

}