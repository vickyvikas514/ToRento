package com.example.torento.USER

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.torento.Adapter.RoomAdapter
import com.example.torento.DATACLASS.Room
import com.example.torento.LOGIN.LandingPage
import com.example.torento.COMMON.Profile
import com.example.torento.R
import com.example.torento.databinding.ActivityMainBinding
import com.example.torento.COMMON.descripn
import com.example.torento.OWNER.owner_home_activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class user_home_activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    val SHARED_PREF : String = "sharedPrefs"

    private var db = Firebase.firestore

    private fun showPopup() {
        val inflater = LayoutInflater.from(this)
        val customLayout = inflater.inflate(R.layout.popup, null)
        // Check if the activity is finishing or has been destroyed
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this)
            builder.setView(customLayout)

            builder.setTitle("Verification Required")
            builder.setMessage("Please verify your email to continue.")
            builder.setCancelable(false)
            val logoutBtn = customLayout.findViewById<Button>(R.id.Logout)
            val sendBtn = customLayout.findViewById<Button>(R.id.BTNSEND)
            val VerifiedBtn = customLayout.findViewById<Button>(R.id.Verifiedemail)
            val dialog = builder.create()
            dialog.show()
            logoutBtn.setOnClickListener{
                logout()
                dialog.dismiss()
            }
            sendBtn.setOnClickListener {
                sendEmailVerification()
            }
            VerifiedBtn.setOnClickListener {
                restartApp(this@user_home_activity)
                dialog.dismiss()
            }
            // Prevent dismiss on outside touch or back button

            // Create and show the dialog using the activity's context

        }
    }
    private fun sendEmailVerification(){
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener{task->
                if(task.isSuccessful){
                    Toast.makeText(baseContext, "Verification email sent.",Toast.LENGTH_SHORT).show()

                } else{
                    Toast.makeText(baseContext, "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
                showPopup()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser?.isEmailVerified==false){
            showPopup()
        }else{
            supportActionBar?.setTitle("Torento")
            actionBar?.hide()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayUseLogoEnabled(true)
            val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
            val username: String? = sharedPreferences.getString("username", "")
            /////////////////////
            DatatoRecyclerView(username)
        }

////////////////////////////////////////////////////////
    }
    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
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
                Toast.makeText(this@user_home_activity, "DocRef is NULL", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception){
            Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
        }
        return@async userId
    }.await()

    override fun onBackPressed() {
        super.onBackPressed()
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finishAndRemoveTask() }
            .setNegativeButton("No", null)
            .show()
    }
    override fun onSupportNavigateUp(): Boolean {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finishAndRemoveTask() }
            .setNegativeButton("No", null)
            .show()
        return true
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
    private fun DatatoRecyclerView(username: String?) {
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
                        lifecycleScope.launch {
                            val Id = withContext(Dispatchers.IO) {
                                getuserId(documentId)
                            }

                            withContext(Main) {
                                val intent = Intent(this@user_home_activity, descripn::class.java)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> logout()
            R.id.profile ->profile()
            R.id.saveditems->SAVEDROOMS()

        }
        return super.onOptionsItemSelected(item)
    }
        private fun SAVEDROOMS(){
            val intent = Intent(this,Save::class.java)
            startActivity(intent)
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