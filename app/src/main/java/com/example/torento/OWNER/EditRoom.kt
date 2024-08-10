package com.example.torento.OWNER

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.torento.DATACLASS.Address
import com.example.torento.DATACLASS.address1
import com.example.torento.R
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO make all the correction in one by hash map and make draft room published
// TODO shows provide all details if i edit one field only and returns from the edit activity
// TODO Locate me is not set up yet
class EditRoom : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var storageRef = Firebase.storage
    val SHARED_PREF: String = "sharedPrefs"
    private var db = com.google.firebase.ktx.Firebase.firestore
    private lateinit var Id:String
    private val imagesList = mutableListOf<Uri>()
    private val imagesListforFirebaseUris = mutableListOf<Uri>()
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private lateinit var userkey: String
    private lateinit var length:String
    private lateinit var width:String
    private lateinit var amount:String
    private lateinit var breif_description:String
    private lateinit var address: address1
    private var check:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addroomtext.text = "Edit your room details"
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username", "").toString()

        Id = intent.getStringExtra("documentid").toString()
        Toast.makeText(this@EditRoom, Id, Toast.LENGTH_SHORT).show()
        binding.uploadbtn.text = "Save the changes"
        binding.setAddressBtn.text = "Edit your address"
        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.pic.setImageURI(it)
            if (it != null) {
                val dpUri = it
                GlobalScope.launch(Dispatchers.IO) {
                    val uploadedImageUri = async { changeDPBG(dpUri) }.await()
                    if (userkey != null) {
                        savechanges(uploadedImageUri,Id,userkey).await()
                        withContext(Dispatchers.Main){
                            binding.progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        binding.dpupdate.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            galleryImage.launch("image/*") }
        binding.uploadbtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (userkey != null) {
                updateTheEdit()
            }else{
                Toast.makeText(this, "username is not found", Toast.LENGTH_SHORT).show()
            }
            //changetohome()
        }
        GlobalScope.launch (Dispatchers.IO){
            retreivingdataBG(Id,userkey.toString())
        }
        val pickImages =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage()
                }}
        binding.AddMorePics.setOnClickListener{
            val options = arrayOf("Remove all the previous images and add new images", "Add some new images to the existing images")

            // Create a dialog for options
            AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            // Choose from Gallery option selected
                            pickImages.launch("image/*")
                        }
                        1 -> {
                            // Take Photo option selected
                            check = 1
                            pickImages.launch("image/*")
                            AddSomeNewImages()
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.setAddressBtn.setOnClickListener {
            showCustomDialog()
        }
    }
    /*private fun ShowDialogForAddmorePics(){
        // Define options in an array
        val options = arrayOf("Remove all the previous images and add new images", "Add some new images to the existing images")

        // Create a dialog for options
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Choose from Gallery option selected
                        RemoveAllImages()
                    }
                    1 -> {
                        // Take Photo option selected
                        AddSomeNewImages()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }*/
    private fun RemoveAllImages(){

        GlobalScope.launch (Dispatchers.IO){
            AddAlltheNewImages()
        }
    }
    private suspend fun AddAlltheNewImages() = GlobalScope.async{


    }.await()
    private fun AddSomeNewImages(){

    }
    private fun uploadImagesToFirebaseStorage() {
        showProgressOverlay(true)
        GlobalScope.launch(Dispatchers.IO) {
            for ((index, imageUri) in imagesList.withIndex()) {
                try {
                    val imageUrl = uploadImage(imageUri)
                    imagesListforFirebaseUris.add(imageUrl)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("ExceptionInAddingMoreImages",e.toString())
                        Toast.makeText(this@EditRoom, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                showProgressOverlay(false)
                Toast.makeText(this@EditRoom, "All images uploaded successfully", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun showProgressOverlay(show: Boolean) {
        //binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isClickable = show
        binding.root.isFocusable = show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    private suspend fun uploadImage(imageUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()
    private fun updateTheEdit(){
         length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
         amount = binding.amount.text.toString()
         breif_description = binding.RoomDescription.text.toString()
        if (!isInputDataValid()) {
            showProgressOverlay(false)
            showDraftRoomConfirmationDialog()
            // Toast.makeText(this, "Please provide all the required details for room", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAddressValid()) {
            showProgressOverlay(false)
            showDraftRoomConfirmationDialog()
            //Toast.makeText(this, "Please provide all the address details", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch (Dispatchers.IO){

            val deferred1 = async {
                    Log.d("CHECKJIJI",length.toString())
                        changeFields(amount, breif_description, length, width,false)
            }
            val deferred7 = async {
            if(check==1){
                appendImagesToFirestore()
            }
            else {
                changeMorePics()
            }
            }
            deferred1.await()
            deferred7.await()

            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@EditRoom, "your Room has been updated", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateTheEditDraft(){
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        amount = binding.amount.text.toString()
        breif_description = binding.RoomDescription.text.toString()
        GlobalScope.launch (Dispatchers.IO){

            val deferred1 = async {
                Log.d("CHECKJIJI",length.toString())
                changeFields(amount, breif_description, length, width,true)
            }
            val deferred7 = async {
                if(check==1){
                    appendImagesToFirestore()
                }
                else {
                    changeMorePics()
                }
            }
            deferred1.await()
            deferred7.await()
            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@EditRoom, "your Room has been updated", Toast.LENGTH_SHORT).show()
                changetohome()
            }
        }
    }
   /* suspend fun changeMeasurements(length:String?,width:String?,Id: String,userkey: String){
        length?.let { db.collection("Rooms").document(Id).update("length",length) }
        length?.let{ db.collection(userkey).document(Id).update("length", length) }
        width?.let{ db.collection("Rooms").document(Id).update("width", width) }
        width?.let{ db.collection(userkey).document(Id).update("width", width) }
    }*/

    suspend fun retreivingdataBG(Id:String,userKey: String){

        if (userKey != null) {

            val roomRef = db.collection(userkey).document(Id)

            roomRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val roomData = documentSnapshot.data

                        // Update EditText fields with existing data
                        GlobalScope.launch (Dispatchers.Main){
                            updateEditTextFields(roomData)
                        }

                    } else {
                        Toast.makeText(this@EditRoom, "Document not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@EditRoom, "Failed to fetch data: $e", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun updateEditTextFields(roomData: Map<String, Any>?) {
        if (roomData != null) {
            // Example: Update length EditText field
             length = (roomData["length"] as? String).toString()
            binding.roomlength.setText(length)
            width = roomData["width"] as? String ?: ""
            binding.roomwidth.setText(width)
            breif_description = roomData["breif_description"] as? String ?: ""
            amount = roomData["amount"] as? String ?: ""
            binding.amount.setText(amount)
            val addressMap = roomData["address"] as? Map<String, Any>
            if (addressMap != null) {
                address = address1.fromMap(addressMap)
            }
            binding.RoomDescription.setText(breif_description)
            //Toast.makeText(this@EditRoom, roomData["imageuri"] as? String, Toast.LENGTH_SHORT).show()
            Glide.with(this)
                .load(roomData["dpuri"] as? String)
                .into(binding.pic)
        }
    }
    suspend fun changeFields(price:String?,descrpn:String?,length:String?,width:String?,isDraft:Boolean){
        val updatedData = hashMapOf(
            "address" to address,
            "amount" to price,
            "breif_description" to descrpn,
            "length" to length,
            "width" to width,

        )
        if(!isDraft) {
            updatedData["status"] = "published"
        }else{
            updatedData["status"] = "draft"
        }
        db.collection(userkey).document(Id).update(updatedData)
        db.collection("Rooms").document(Id).update(updatedData)
    }
    suspend fun changeMorePics(){
        imagesListforFirebaseUris?.let{
            db.collection(userkey).document(Id).update("imageuri", imagesListforFirebaseUris)
            db.collection("Rooms").document(Id).update("imageuri", imagesListforFirebaseUris)
        }
    }
    suspend fun savechanges(uploadedImageUri:Uri,Id:String,userkey:String): Deferred<Unit> = GlobalScope.async{
       db.collection("Rooms").document(Id).update("dpuri" , uploadedImageUri)
        db.collection(userkey).document(Id).update("dpuri" , uploadedImageUri)

    }
    suspend fun changeDPBG(dpUri:Uri) : Uri{
        if (dpUri != null) {
            try {
                val imageRef = storageRef.getReference("Dpimages").child(System.currentTimeMillis().toString())
                val uploadTask = imageRef.putFile(dpUri)

                val imageUrl = Tasks.await(uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                })

                return imageUrl
            } catch (e: Exception) {
                Log.e("ErrorinER", "Failed to change DP image: $e")
            }
        } else {
            Log.e("ErrorinER", "dpUri is NULL")
        }

        return Uri.EMPTY
    }
    override fun onBackPressed() {
        showDeleteRoomConfirmationDialog() // Call the confirmation dialog when the back button is pressed
    }
    private fun showDeleteRoomConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Your room deatils are not saved yet. Are you sure you want to leave your progress?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                GlobalScope.launch (IO){
                    changetohome()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun showDraftRoomConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("All the room details are not filled yet. Do you want to save these changes in Drafted room")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                GlobalScope.launch (IO){
                    updateTheEditDraft()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                showDeleteRoomConfirmationDialog()
                dialog.dismiss()
            }
            .show()
    }
    private fun changetohome(){
        /*if (!isInputDataValid()) {
            //showProgressOverlay(false)
            //Toast.makeText(this, "Please provide all the required details for room", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAddressValid()) {
            //showProgressOverlay(false)
            //Toast.makeText(this, "Please provide all the address details", Toast.LENGTH_SHORT).show()
            return
        }*/
        val intent = Intent(this@EditRoom, owner_home_activity::class.java)
        startActivity(intent)
        finish()
    }
    private suspend fun appendImagesToFirestore() {
        val roomRef = db.collection("Rooms").document(Id)
        val userRef = db.collection(userkey).document(Id)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                val existingImages = snapshot.get("imageuri") as? MutableList<String> ?: mutableListOf()

                imagesListforFirebaseUris.forEach {
                    existingImages.add(it.toString())
                }

                transaction.update(roomRef, "imageuri", existingImages)
                transaction.update(userRef, "imageuri", existingImages)
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreError", "Failed to append images: $e")
        }
    }
    private fun showCustomDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogLayout = inflater.inflate(R.layout.select_address_popup, null)
        val dropdownMenu1 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu1)
        val dropdownMenu2 = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu2)

        dialogLayout.findViewById<EditText>(R.id.locality).setText(address.locality)
        dialogLayout.findViewById<EditText>(R.id.house_no).setText(address.house_no)


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
                address.state = states[position]
                val districts = districtsMap[address.state] ?: emptyList()
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

        val pincode1 = dialogLayout.findViewById<EditText>(R.id.pincode)
        pincode1.setText(address.pincode)
        pincode1.addTextChangedListener(object : TextWatcher {
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
                    pincode1.setText(s?.subSequence(0, 6))
                    pincode1.setSelection(6) // Move cursor to the end
                }
            }
        })

        val set_address_btn = dialogLayout.findViewById<Button>(R.id.set_address_btn)
        set_address_btn.setOnClickListener {
            address.state = dropdownMenu1.selectedItem.toString()
            address.district = dropdownMenu2.selectedItem.toString()
            address.locality = dialogLayout.findViewById<EditText>(R.id.locality).text.toString()
            address.pincode = dialogLayout.findViewById<EditText>(R.id.pincode).text.toString()
            address.house_no = dialogLayout.findViewById<EditText>(R.id.house_no).text.toString()
            if(address.state.isEmpty() || address.district.isEmpty() || address.locality.isEmpty() || address.pincode.isEmpty() || address.house_no.isEmpty()){
                Toast.makeText(this@EditRoom, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }else{
                dialog.dismiss()

                Toast.makeText(this@EditRoom, "Your address is set", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
    }
    private fun isAddressValid(): Boolean {
        return try {
            if (address.state.isBlank() || address.district.isBlank() || address.locality.isBlank() || address.pincode.isBlank() || address.house_no.isBlank()) { // Check if 'state' is initialized and not empty
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
    private fun isInputDataValid(): Boolean {
        // Validate all the necessary input fields here
        return try {
            if (length.isBlank() || width.isBlank() ||
                amount.isBlank() || breif_description.isBlank()
               ) { // Check if 'state' is initialized and not empty
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