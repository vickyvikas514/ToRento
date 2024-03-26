package com.example.torento.OWNER

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.torento.Adapter.RoomAdapter
import com.example.torento.LOGIN.LandingPage
import com.example.torento.LOGIN.LandingPage.Companion.num
import com.example.torento.LOGIN.LandingPage.Companion.userid
import com.example.torento.COMMON.Profile
import com.example.torento.R
import com.example.torento.DATACLASS.Room
import com.example.torento.databinding.ActivityOwnerHomeBinding
import com.example.torento.COMMON.descripn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class owner_home_activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityOwnerHomeBinding
    val SHARED_PREF : String = "sharedPrefs"
    var x=0
    private var db = Firebase.firestore
    private lateinit var job: Job

    var id=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        job = Job()
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        auth = FirebaseAuth.getInstance()
        binding.addButton.setOnClickListener{
            //made temp room
            if (savedInstanceState != null) {
                // Restore the values from the saved state
                x = savedInstanceState.getInt("x", 0)
                num = savedInstanceState.getInt("num", 0)
            }
            GlobalScope.launch {
            if (userkey != null) {
                userid = userkey
            }
              addtemproom()
            }
        }
        supportActionBar?.setTitle("Torento")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.brown)))

        val itemsCollection = userkey?.let { db.collection(it) }
        val itemsList = mutableListOf<Room>()
        val idlist = mutableListOf<String>()

        if (itemsCollection != null) {
            itemsCollection.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle the error
                    return@addSnapshotListener
                }
                snapshot?.forEach { document ->
                    val roomimage = document.getString("dpuri")?:""
                    val description = document.getString("location") ?: ""
                    val roomlength = document.getString("length") ?: ""
                    val roomwidth = document.getString("width") ?: ""
                    val roomsize:String = roomlength+"x"+roomwidth
                    val Docid:String = document.id
                    val item = Room( roomsize, description,roomimage)
                    idlist.add(Docid)
                    itemsList.add(item)
                }

                var adapter = RoomAdapter(
                    applicationContext,
                    itemsList,
                    idlist
                )
                binding.OwnerRoomlist.adapter = adapter
                adapter.setOnItemClickListener(object : RoomAdapter.OnItemClickListener{
                    override fun onItemClick(documentid:String,position: Int) {

                        // Handle item click here
                        // For example, navigate to another activity
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val userId = userkey?.let { it1 -> getuserId(it1) }

                                launch (Dispatchers.Main){
                                    val intent = Intent(this@owner_home_activity, descripn::class.java)
                                    intent.putExtra("documentid",documentid)
                                    intent.putExtra("usertype","owner")
                                    intent.putExtra("collection2",userkey)
                                    intent.putExtra("userIdO","0D2bMnHrhcWCSkyRlklWMhY0NTS2")
                                    startActivity(intent)
                                }

                            }catch (e:Exception){
                                e.printStackTrace()
                            }

                        }

                    }
                })
            }

            binding.OwnerRoomlist.setHasFixedSize(true)
        }

        }
    suspend fun getuserId(userkey:String): String = GlobalScope.async{

        var userId:String = ""
        try {
            val docref = db.collection("users").document(userkey).get().await()
            if (docref != null) {
                docref.data?.let {
                    userId = it["userId"].toString()
                }
            }

            else {
                Toast.makeText(this@owner_home_activity, "DocRef is NULL", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception){
            Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
        }
        return@async userId
    }.await()
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the job when the activity is destroyed
        job.cancel()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar,menu)
        val itemToHide = menu!!.findItem(R.id.saveditems)
        itemToHide.setVisible(false)
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
        editor.apply()
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
    private fun addtemproom(){

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val userkey: String? = sharedPreferences.getString("username", "")
        val collectionReference = db.collection("Rooms")
        collectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                // Get the count of documents in the collection

                num = querySnapshot.size()+1

                Log.d("vicky","$x")
                subaddtemproom(userkey,num)
                changepage()

            }
            .addOnFailureListener { e ->
                num=1
                x=1
                Toast.makeText(this, "fail in room count", Toast.LENGTH_SHORT).show()

            }
        Log.d("vicky1","$num")
       


    }
    private fun subaddtemproom(userkey: String?, x: Int) {
        val room = hashMapOf(
            "length" to "temp",
            "width" to "temp",
            "location" to "temp",
            "imageuri" to "temp",
            "dpuri" to "temp",
            "location_detail" to "temp",
            "owner_name" to "temp",
            "amount" to "temp",
            "breif_description" to "temp",
            "ownerId" to auth.currentUser?.uid ?: return
        )
        if (userkey != null) {
            if (userkey.isNotEmpty()){
                Log.d("vicky2","$x")
                db.collection("Rooms").document(userkey+"$x")
                    .set(room)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Temp Room is set", Toast.LENGTH_SHORT).show()
                        Log.d("vikas", "Success $userkey$x")
                    }
                    .addOnFailureListener { e ->
                        Log.w("vikas", "Error adding document", e)
                    }

                db.collection(userkey).document(userkey+"$x")
                    .set(room)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Temp Room is set for owner", Toast.LENGTH_SHORT).show()
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
    }
    private fun changepage() {
        val intent = Intent(this, add_room::class.java)
        startActivity(intent)
    }
    //to save values of x and num in changing configuration
    override fun onSaveInstanceState(outState: Bundle) {
        // Save the values of your variables
        outState.putInt("x", x)
        outState.putInt("num", num)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Restore the values after the activity has been recreated
        super.onRestoreInstanceState(savedInstanceState)

        x = savedInstanceState.getInt("x", 0)
        num = savedInstanceState.getInt("num", 0)
    }

}