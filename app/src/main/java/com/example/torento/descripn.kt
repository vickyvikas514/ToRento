package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torento.databinding.ActivityDescripnBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
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

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        val usertype = intent.getStringExtra("usertype")
        if(usertype=="owner"){
            binding.saveBtn.text = "Change Room details"
        }
    //fetchDataFromFirestore
      set()
        binding.saveBtn.setOnClickListener {
            if(usertype=="owner"){
                startActivity(Intent(this@descripn,EditRoom::class.java))
                finish()
            }else{
                Toast.makeText(this@descripn, "clicked", Toast.LENGTH_SHORT).show()
            }
        }
        binding.listPhoto.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

           }
    suspend fun getusertype(userkey:String): String = GlobalScope.async{

        var usertype:String = ""
        try {
            val docref = db.collection("users").document(userkey).get().await()
            if (docref != null) {
                docref.data?.let {
                    usertype = it["usertype"].toString()
                }
            }

            else {
                Toast.makeText(this@descripn, "DocRef is NULL", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception){
            Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
        }
        return@async usertype
    }.await()
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



}