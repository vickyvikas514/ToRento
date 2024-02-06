package com.example.torento.USER

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.torento.Adapter.RoomAdapter
import com.example.torento.COMMON.descripn
import com.example.torento.DATACLASS.Room
import com.example.torento.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Save  : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val SHARED_PREF : String = "sharedPrefs"
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val username: String? = sharedPreferences.getString("username", "")
        DatatoRecyclerView(username)
    }
    suspend fun retreivingdata(username: String?): Pair<List<Room>, List<String>> = withContext(Dispatchers.IO){
        val itemsCollection = db.collection(username.toString())
        val itemsList = mutableListOf<Room>()
        val idlist = mutableListOf<String>()
        if (itemsCollection != null) {
            itemsCollection.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle the error
                    return@addSnapshotListener
                }
                snapshot?.forEach { document ->
                    val roomimage = document.getString("dpuri") ?: ""
                    val description = document.getString("location") ?: ""
                    val roomlength = document.getString("length") ?: ""
                    val roomwidth = document.getString("width") ?: ""
                    val roomsize: String = roomlength + "x" + roomwidth
                    val Docid:String = document.id
                    val item = Room(roomsize, description, roomimage)
                    itemsList.add(item)
                    idlist.add(Docid)
                }
            }
        }
        delay(1000)
        return@withContext Pair(itemsList,idlist)
    }
    suspend fun getuserId(userkey:String): String = GlobalScope.async{

        var userId:String = ""
        try {
            val docref = db.collection("Rooms").document(userkey).get().await()
            if (docref != null) {
                docref.data?.let {
                    userId = it["ownerId"].toString()
                }
            }

            else {
                Toast.makeText(this@Save, "DocRef is NULL", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception){
            Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
        }
        return@async userId
    }.await()
    private fun DatatoRecyclerView(username: String?) {
        GlobalScope.launch {
            val (rooms, ids) = async { retreivingdata(username) }.await()
            withContext(Dispatchers.Main){
                val adapter = RoomAdapter(
                    applicationContext,
                    rooms,
                    ids
                )
                binding.Roomlist.adapter = adapter
                binding.Roomlist.setHasFixedSize(true)
                adapter.setOnItemClickListener(object : RoomAdapter.OnItemClickListener {
                    override fun onItemClick(documentId: String, position: Int) {
                        lifecycleScope.launch {
                            val Id = withContext(Dispatchers.IO) {
                                getuserId(documentId)
                            }

                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@Save, descripn::class.java)
                                intent.putExtra("documentid", documentId)
                                intent.putExtra("usertype", "user")
                                intent.putExtra("userId", Id)
                                intent.putExtra("username", username)
                                startActivity(intent)
                            }
                        }

                    }
                })
            }
        }

    }
}