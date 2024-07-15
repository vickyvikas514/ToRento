package com.example.torento.USER

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
    private var selected_district: String = ""
    private lateinit var username: String
    private var db = Firebase.firestore



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
            DatatoRecyclerView(username)
        }

////////////////////////////////////////////////////////
        binding.addressSelect.setOnClickListener { showCustomDialog() }
        /*val optionsForFirstSpinner = listOf("Option 1", "Option 2", "Option 3")

        // Sample data for the second spinner based on first spinner selection
        val optionsForSecondSpinnerMap = mapOf(
            "Option 1" to listOf("Option 1.1", "Option 1.2", "Option 1.3"),
            "Option 2" to listOf("Option 2.1", "Option 2.2", "Option 2.3"),
            "Option 3" to listOf("Option 3.1", "Option 3.2", "Option 3.3")
        )
        val firstSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, optionsForFirstSpinner)
        firstSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dropdownMenu1.adapter = firstSpinnerAdapter

        // Set up the adapter for the second spinner
        val secondSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf<String>())
        secondSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dropdownMenu2.adapter = secondSpinnerAdapter
        binding.dropdownMenu1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedOption = optionsForFirstSpinner[position]
                val secondSpinnerOptions = optionsForSecondSpinnerMap[selectedOption] ?: emptyList()
                secondSpinnerAdapter.clear()
                secondSpinnerAdapter.addAll(secondSpinnerOptions)
                secondSpinnerAdapter.notifyDataSetChanged()
                binding.dropdownMenu2.isEnabled = secondSpinnerOptions.isNotEmpty()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle case when nothing is selected
                binding.dropdownMenu2.isEnabled = false
            }
        }*/

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
                    val roomOwnerDpUrl = document.getString("ownerDpUrl") ?: ""
                    val Docid:String = document.id
                    val item = Room(roomsize, description, roomimage, roomOwnerDpUrl)
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
    private fun showCustomDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogLayout = inflater.inflate(R.layout.select_address_popup, null)
        val dropdownMenu1 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu1)
        val dropdownMenu2 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu2)

        val stateDistrictData = Address.getDefaultData()
        val states = stateDistrictData.states
        val districtsMap = stateDistrictData.districtsMap


        val firstSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        firstSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdownMenu1.adapter = firstSpinnerAdapter

        val mutableOptionsForSecondSpinner = mutableListOf<String>()
        val secondSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableOptionsForSecondSpinner)
        secondSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
            selected_state = dropdownMenu1.selectedItem.toString()
            selected_district = dropdownMenu2.selectedItem.toString()
            val locality = dialogLayout.findViewById<EditText>(R.id.locality).text.toString()
            val pincode = dialogLayout.findViewById<EditText>(R.id.pincode).text.toString()
            val house_no = dialogLayout.findViewById<EditText>(R.id.house_no).text.toString()
            if(selected_state.isEmpty() || selected_district.isEmpty()){
                Toast.makeText(this@user_home_activity, "Please select state and district respectively from the given list", Toast.LENGTH_SHORT).show()
            }else{
                Add_Address(selected_state,selected_district,locality,pincode,house_no,dialog)
            }
        }


        dialog.show()
    }
    private fun Add_Address(
        selected_state: String,
        selected_district: String,
        locality: String,
        pincode: String,
        house_no: String,
        dialog: AlertDialog
    ) {
        val address = hashMapOf(
            "state" to selected_state,
            "district" to selected_district,
            "locality" to locality,
            "pincode" to pincode,
            "house_no" to house_no,
        )
        db.collection("users").document(username).update("address",address)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this@user_home_activity, "Address is set successfully", Toast.LENGTH_SHORT).show()
            }
    }
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

