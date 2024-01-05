package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.torento.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

                var adapter = RoomAdapter(
                    applicationContext,
                    itemsList,
                    idlist
                )
                binding.Roomlist.adapter = adapter
                adapter.setOnItemClickListener(object :RoomAdapter.OnItemClickListener{
                    override fun onItemClick(documentid:String,position: Int) {

                        // Handle item click here
                        // For example, navigate to another activity
                        val intent = Intent(this@user_home_activity, descripn::class.java)
                        intent.putExtra("documentid",documentid)
                        startActivity(intent)
                    }
                })

            }

            binding.Roomlist.setHasFixedSize(true)
        }
////////////////////////////////////////////////////////
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()

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
        editor.putString("usertype","")
        editor.apply()
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}