package com.example.torento.USER

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.torento.Adapter.RoomAdapter
import com.example.torento.DATACLASS.Room
import com.example.torento.LOGIN.LandingPage
import com.example.torento.COMMON.Profile
import com.example.torento.R
import com.example.torento.databinding.ActivityMainBinding
import com.example.torento.COMMON.descripn
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class user_home_activity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val SHARED_PREF : String = "sharedPrefs"

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setTitle("Torento")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        /////////////////////
        DatatoRecyclerView()
////////////////////////////////////////////////////////
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()

    }
   suspend fun retreivingdata() : Pair<List<Room>, List<String>> = withContext(Dispatchers.IO){
        val itemsCollection = db.collection("Rooms")
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
            kotlinx.coroutines.delay(1000)
            return@withContext Pair(itemsList,idlist)
    }
    private fun DatatoRecyclerView(){
        GlobalScope.launch {
            val (rooms, ids) = async { retreivingdata() }.await()
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
                        val intent = Intent(this@user_home_activity, descripn::class.java)
                        intent.putExtra("usertype","user")
                        startActivity(intent)
                    }
                })
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> logout()
            R.id.profile ->profile()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun profile() {

        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
    }

    private fun logout(){
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF,
            MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("name","false")
        editor.putString("username","")
        editor.putString("usertype","")
        editor.apply()
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}