package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        val itemsCollection = db.collection("owners")
        val itemsList = mutableListOf<Room>()
        binding.Roomlist.adapter = RoomAdapter(
            applicationContext,
            itemsList
        )

        itemsCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            snapshot?.forEach { document ->
                val roomimage = document.getString("roomimage")?:""
                val description = document.getString("roomdescription") ?: ""
                val roomsize = document.getString("roomsize") ?: ""
                val item = Room( roomsize, description,roomimage)
                itemsList.add(item)
            }

            binding.Roomlist.adapter = RoomAdapter(
                applicationContext,
                itemsList
            )
        }
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_between_cards) // Adjust the dimension as needed
        val maxVisibleItems = 3 // Adjust the number of visible items as needed
        val cardHeight = resources.getDimensionPixelSize(R.dimen.card_height) // Define in dimens.xml


        binding.Roomlist.addItemDecoration(CustomItemDecoration(maxVisibleItems, cardHeight))
        binding.Roomlist.setHasFixedSize(true)

    }
    class CustomItemDecoration(
        private val maxVisibleItems:Int,
        private val cardHeight:Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: android.view.View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if(position>=maxVisibleItems){
                outRect.top = 0
            }else{
                val screenHeight = parent.height
                val totalCardHeight = maxVisibleItems * cardHeight
                val availableHeight = screenHeight - totalCardHeight
                if (position == maxVisibleItems - 1) {
                    // Last visible item gets the remaining space to fill the screen
                    outRect.top = availableHeight
                } else {
                    // Margin for other visible items
                    outRect.top = 0
                }
            }

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