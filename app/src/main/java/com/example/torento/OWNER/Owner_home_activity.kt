package com.example.torento.OWNER

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.torento.Adapter.RoomAdapter
import com.example.torento.COMMON.Profile
import com.example.torento.COMMON.descripn
import com.example.torento.DATACLASS.Room
import com.example.torento.DATACLASS.address1
import com.example.torento.LOGIN.LandingPage
import com.example.torento.LOGIN.LandingPage.Companion.userid
import com.example.torento.R
import com.example.torento.databinding.ActivityOwnerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class owner_home_activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityOwnerHomeBinding
    val SHARED_PREF : String = "sharedPrefs"
    private var db = Firebase.firestore
    private var job: Job = Job()
    var id=""
    private var test = ""
    private lateinit var AddBtnAnimation: LottieAnimationView
    private lateinit var address: address1
    private var isViewingDrafts = false
    private lateinit var owner_Id:String
    private lateinit var username:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AddBtnAnimation = binding.addButton
        AddBtnAnimation.playAnimation()
        auth = FirebaseAuth.getInstance()
        owner_Id = auth.currentUser?.uid.toString()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            user?.let {
                if (user.isEmailVerified) {
                    // If email is verified, update SharedPreferences
                    updateVerificationStatusInSharedPreferences(true)
                }
            }
        }
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            if (!user.isEmailVerified) {
                // User's email is not verified, prompt them to verify it
                // You can show a dialog or display a message in your UI
                showPopup()
            }
        }


// Implement AuthStateListener to handle authentication state changes
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            user?.let { currentUser ->
                if (!currentUser.isEmailVerified) {
                    // User's email is not verified, prompt them to verify it
                    // You can show a dialog or display a message in your UI
                    showPopup()
                }
            }
        }

// Add the AuthStateListener to the FirebaseAuth instance
        auth.addAuthStateListener(authStateListener)
            job = Job()
            val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
             username = sharedPreferences.getString("username", "").toString()
            userid = username.toString()
            binding.addButton.setOnClickListener{
                //made temp room

                    GlobalScope.launch {
                    if (username != null) {
                        userid = username
                    }
                    changepage()
                }
            }
            supportActionBar?.setTitle("KAMRE")
            actionBar?.hide()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayUseLogoEnabled(true)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.brown)))

            /*val itemsCollection = userkey?.let { db.collection(it) }
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
                        val addressMap = document["address"] as? Map<String, Any>
                        if (addressMap != null) {
                            address = address1.fromMap(addressMap)
                        }
                        val roomlength = document.getString("length") ?: ""
                        val roomwidth = document.getString("width") ?: ""
                        val roomsize:String = roomlength+" ft"+" x "+roomwidth+" ft"
                        val roomOwnerDpUrl:String = document.getString("ownerDpUrl")?:""
                        val Docid:String = document.id
                        val item = Room( roomsize, address.locality,roomimage, roomOwnerDpUrl)
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
                                    launch (Dispatchers.Main){
                                        Toast.makeText(this@owner_home_activity, test, Toast.LENGTH_SHORT).show()
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
            }*/
        fetchRooms("draft")


        }
    private fun fetchRooms(status:String) {
        val itemsCollection = db.collection(owner_Id)
        val itemsList = mutableListOf<Room>()
        val idlist = mutableListOf<String>()

        itemsCollection.whereNotEqualTo("status", status).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            itemsList.clear()
            idlist.clear()

            snapshot?.forEach { document ->
                val roomimage = document.getString("dpuri") ?: ""
                val addressMap = document["address"] as? Map<String, Any>
                if (addressMap != null) {
                    address = address1.fromMap(addressMap)
                }
                val roomlength = document.getString("length") ?: ""
                val roomwidth = document.getString("width") ?: ""
                val roomsize = "$roomlength ft x $roomwidth ft"
                val roomOwnerDpUrl = document.getString("ownerDpUrl") ?: ""
                val Docid = document.id
                val item = Room(roomsize, address.locality, roomimage, roomOwnerDpUrl)
                idlist.add(Docid)
                itemsList.add(item)
            }

            val adapter = RoomAdapter(
                applicationContext,
                itemsList,
                idlist
            )
            binding.OwnerRoomlist.adapter = adapter
            adapter.setOnItemClickListener(object : RoomAdapter.OnItemClickListener {
                override fun onItemClick(documentid: String, position: Int) {
                    // Handle item click here
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            launch(Dispatchers.Main) {
                                Toast.makeText(this@owner_home_activity, test, Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@owner_home_activity, descripn::class.java)
                                intent.putExtra("documentid", documentid)
                                intent.putExtra("usertype", "owner")
                                intent.putExtra("ownerId", owner_Id)
                                intent.putExtra("username", username)
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }

        binding.OwnerRoomlist.setHasFixedSize(true)
    }

    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
    private fun updateVerificationStatusInSharedPreferences(isEmailVerified: Boolean) {
        val sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isEmailVerified", isEmailVerified)
        editor.apply()
    }
    override fun onBackPressed() {

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
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the job when the activity is destroyed
        job.cancel()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar,menu)
        
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val draftrooms = menu?.findItem(R.id.saveditems)
        draftrooms?.title = if (isViewingDrafts) "Listed Rooms" else "Draft Rooms"
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> logout()
            R.id.profile ->profile()
            R.id.saveditems-> toggleRoomsView()

        }
        return super.onOptionsItemSelected(item)
    }
    private fun toggleRoomsView() {
        isViewingDrafts = !isViewingDrafts
        val status = if (isViewingDrafts) "published" else "draft"
        fetchRooms(status)
        invalidateOptionsMenu() // This will trigger onPrepareOptionsMenu to update the menu item title
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
    private fun changepage() {
        val intent = Intent(this, add_room::class.java)
        startActivity(intent)
    }
    //to save values of x and num in changing configuration
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
                restartApp(this@owner_home_activity)
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
                //showPopup()
            }
    }
    /*private fun dismissPopup() {
        popupWindow.dismiss()
    }*/
    }