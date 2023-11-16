package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.torento.databinding.ActivityMainBinding
import com.example.torento.databinding.ActivityOwnerHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class owner_home_activity : AppCompatActivity() {
    private lateinit var binding: ActivityOwnerHomeBinding
    val SHARED_PREF : String = "sharedPrefs"

    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addButton.setOnClickListener{
            val intent = Intent(this,add_room::class.java)
            startActivity(intent)
        }
        supportActionBar?.setTitle("Torento")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        val itemsCollection = userkey?.let { db.collection(it) }
        val itemsList = mutableListOf<Room>()
        binding.OwnerRoomlist.adapter = RoomAdapter(
            applicationContext,
            itemsList
        )

        if (itemsCollection != null) {
            itemsCollection.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle the error
                    return@addSnapshotListener
                }
                snapshot?.forEach { document ->
                    val roomimage = document.getString("imageuri")?:""
                    val description = document.getString("location") ?: ""
                    val roomlength = document.getString("length") ?: ""
                    val roomwidth = document.getString("width") ?: ""
                    val roomsize:String = roomlength+"x"+roomwidth
                    val item = Room( roomsize, description,roomimage)
                    itemsList.add(item)
                }

                binding.OwnerRoomlist.adapter = RoomAdapter(
                    applicationContext,
                    itemsList
                )
            }

            binding.OwnerRoomlist.setHasFixedSize(true)
        }

        }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout-> logout()
            R.id.profile->profile()

        }
        return super.onOptionsItemSelected(item)
    }
    private fun profile() {

        val intent = Intent(this,Profile::class.java)
        startActivity(intent)
    }
    private fun logout(){
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF,
            MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("name","false")
        editor.putString("username","")
        editor.apply()
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}