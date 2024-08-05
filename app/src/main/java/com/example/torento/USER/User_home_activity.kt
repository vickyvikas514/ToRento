package com.example.torento. USER

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import com.example.torento.DATACLASS.Address
import com.example.torento.DATACLASS.address1
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
//TODO check drop down menus
class user_home_activity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    val SHARED_PREF : String = "sharedPrefs"
    private var selected_state: String = ""

    private lateinit var username: String
    private var db = Firebase.firestore
    private var address: address1? = null
    private lateinit var addresstoset: address1
    private var IsViewingLiked = false




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
            username = sharedPreferences.getString("username", "") ?: ""
            /////////////////////
            DatatoRecyclerView(username,false)
        }

////////////////////////////////////////////////////////
        binding.addressSelect.setOnClickListener { showCustomDialog() }


    }
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

   suspend fun retreivingdata(Liked:Boolean) : Pair<List<Room>, List<String>> = withContext(Dispatchers.IO){

       val itemsCollection = if(Liked)db.collection(username) else db.collection("Rooms")
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
                    val item = address?.let { Room( roomsize, it.locality,roomimage, roomOwnerDpUrl) }
                    idlist.add(Docid)
                    if (item != null) {
                        itemsList.add(item)
                    }
                }
               }
             }
            kotlinx.coroutines.delay(1000)
            return@withContext Pair(itemsList,idlist)
    }
    private fun DatatoRecyclerView(username: String?,Liked:Boolean) {
        GlobalScope.launch {
            val (rooms, ids) = async { retreivingdata(Liked) }.await()
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
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val draftrooms = menu?.findItem(R.id.saveditems)
        draftrooms?.title = if (IsViewingLiked) "All the Rooms" else "Liked Rooms"
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> logout()
            R.id.profile ->profile()
            R.id.saveditems->toggleRoomsView()

        }
        return super.onOptionsItemSelected(item)
    }
    private fun toggleRoomsView() {
        IsViewingLiked = !IsViewingLiked
        val status = if (IsViewingLiked) true else false
        DatatoRecyclerView(username, status)
        invalidateOptionsMenu() // This will trigger onPrepareOptionsMenu to update the menu item title
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
    private fun showCustomDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogLayout = inflater.inflate(R.layout.select_address_popup, null)
        val dropdownMenu1 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu1)
        val dropdownMenu2 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu2)

        addresstoset = address1(
            state = "",
            district = "",
            locality = "",
            pincode = "",
            house_no = ""
        )

        val stateDistrictData = Address.getDefaultData()
        val states = stateDistrictData.states
        val districtsMap = stateDistrictData.districtsMap


        val firstSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        firstSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        dropdownMenu1.adapter = firstSpinnerAdapter

        val mutableOptionsForSecondSpinner = mutableListOf<String>()
        val secondSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableOptionsForSecondSpinner)
        secondSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        dropdownMenu2.adapter = secondSpinnerAdapter

        dropdownMenu1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                 selected_state = states[position]
                val districts = districtsMap[selected_state] ?: emptyList()
                mutableOptionsForSecondSpinner.clear()
                mutableOptionsForSecondSpinner.addAll(districts)
                secondSpinnerAdapter.notifyDataSetChanged()
                dropdownMenu2.isEnabled = districts.isNotEmpty()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                dropdownMenu2.isEnabled = false
            }

        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogLayout)
            .create()

        val pincode = dialogLayout.findViewById<EditText>(R.id.pincode)

        pincode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action required here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 6) {
                    hideKeyboard(dialogLayout)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 6) {
                    // Truncate the input to 10 digits if exceeded
                    pincode.setText(s?.subSequence(0, 6))
                    pincode.setSelection(6) // Move cursor to the end
                }
            }
        })

        val set_address_btn = dialogLayout.findViewById<Button>(R.id.set_address_btn)
        set_address_btn.setOnClickListener {
            addresstoset.state = dropdownMenu1.selectedItem.toString()
            addresstoset.district = dropdownMenu2.selectedItem.toString()
            addresstoset.locality = dialogLayout.findViewById<EditText>(R.id.locality).text.toString()
            addresstoset.pincode = dialogLayout.findViewById<EditText>(R.id.pincode).text.toString()
            addresstoset.house_no = dialogLayout.findViewById<EditText>(R.id.house_no).text.toString()
            if(isAddressValid()){
                Add_Address(dialog)
            }else{
                Toast.makeText(this@user_home_activity, "Please select state and district respectively from the given list", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
    }
    private fun Add_Address(

        dialog: AlertDialog
    ) {
        db.collection("users").document(username).update("address",addresstoset)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this@user_home_activity, "Address is set successfully", Toast.LENGTH_SHORT).show()
            }
    }
    private fun isAddressValid(): Boolean {
        return try {
            if (addresstoset.state.isBlank() || addresstoset.district.isBlank() ||
                addresstoset.locality.isBlank() || addresstoset.pincode.isBlank()
                || addresstoset.house_no.isBlank()) { // Check if 'state' is initialized and not empty
                //Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                false
            } else {
                // Perform your actual address validation logic here
                true
            }
        } catch (e: UninitializedPropertyAccessException) {
            //Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

