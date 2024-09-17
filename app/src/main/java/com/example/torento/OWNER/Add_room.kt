package com.example.torento.OWNER

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.torento.DATACLASS.Address
import com.example.torento.R
import com.example.torento.databinding.ActivityAddRoomBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

// TODO 719 pe upload BG call ho raha hai par agar dp select karni hi nhi fir yeh function call hi nhi hoga aur draft save nhi hoga

class add_room : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoomBinding
    private var db = Firebase.firestore
    private var storageRef = Firebase.storage
    private lateinit var length:String
    private lateinit var width:String
    private  var state:String=""
    private  var district:String=""
    private  var locality:String = ""
    private  var house_no:String = ""
    private  var pincode:String = ""
    private lateinit var amount:String
    private lateinit var owner_name:String
    private lateinit var breif_description:String
    private var dpUri : String = ""
    private var roomId:String=""
    val SHARED_PREF : String = "sharedPrefs"
    private var userkey:String? = ""
    private lateinit var dpuri:Uri
    private val storage = FirebaseStorage.getInstance()
    private val storageref: StorageReference = storage.reference
    private val imagesList = mutableListOf<Uri>()
    private val imagesListforFirebaseUris = mutableListOf<Uri>()
    private lateinit var auth: FirebaseAuth
    private lateinit var roomOwnerDpUrl:String
    private lateinit var loadingAnimation :LottieAnimationView
    private lateinit var touchInterceptor:View
    private lateinit var ownerId:String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var shouldCheckLocationPermission = false
    private var addressDialog: AlertDialog? = null




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storageRef = FirebaseStorage.getInstance()
        loadingAnimation = findViewById(R.id.progressBar)
        touchInterceptor = binding.touchInterceptor

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        userkey = sharedPreferences.getString("username", "")
        Toast.makeText(this, userkey, Toast.LENGTH_SHORT).show()
        //Requesting location permission
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launch(IO) {
//            GlobalScope.launch(Dispatchers.Main){
//                Toast.makeText(this@add_room, "11", Toast.LENGTH_SHORT).show()
//            }

            roomOwnerDpUrl = getOwnerDp()
        }

        val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.isNotEmpty()) {
                    imagesList.addAll(uris)
                    // Upload images to Firebase Storage
                    uploadImagesToFirebaseStorage()
                }}

        auth = FirebaseAuth.getInstance()
        ownerId = auth.currentUser?.uid.toString()

        binding.uploadbtn.setOnClickListener {
            //Toast.makeText(this, "HELLO", Toast.LENGTH_SHORT).show()
              UploadTheRoom()
        }

        binding.setAddressBtn.setOnClickListener { showCustomDialog() }

        binding.dpupdate.visibility = View.GONE

        binding.picCard.setOnClickListener{
           showImageSourceOptions()
        }
        binding.AddMorePics.setOnClickListener {
            hideKeyboard(this@add_room)
            if (this@add_room::dpuri.isInitialized && dpuri != "".toUri()){
                pickImages.launch("image/*")
            }else{
                Toast.makeText(this, "Please Select an image first for your room", Toast.LENGTH_SHORT).show()
            }

        }
        supportActionBar?.setTitle("KAMRE")
        actionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.brown)))

    }

    private fun onLocateMeClicked() {
        shouldCheckLocationPermission = true
        checkLocationPermission()
    }
    private fun isLocationEnabled(): Boolean {
        Toast.makeText(this, "check location", Toast.LENGTH_SHORT).show()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }
    private fun checkLocationPermission() {
        // Request permission if not granted, or get the current location
        if(!isLocationEnabled()){
            showLocationEnableDialog()
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }
    private fun showLocationEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Your location is turned off. Please enable location services to detect your address.")
            .setPositiveButton("Enable") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                Toast.makeText(this, "Location is required to detect address", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }
    override fun onResume() {
        super.onResume()
        if (shouldCheckLocationPermission) {
            checkLocationPermission()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses: List<android.location.Address>? = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressText = "${address.getAddressLine(0)}+ ${address.locality}+ ${address.adminArea}+ ${address.countryName}"
                            //60, gali no3, ghukna then Ghaziabad then Uttar Pradesh then India
                            val stateTextView = addressDialog?.findViewById<TextView>(R.id.state)
                            val districtTextView = addressDialog?.findViewById<TextView>(R.id.District)
                            val localityEditText = addressDialog?.findViewById<EditText>(R.id.locality)
                            val houseNoEditText = addressDialog?.findViewById<EditText>(R.id.house_no)
                            val pincodeEditText = addressDialog?.findViewById<EditText>(R.id.pincode)
                            val statelist = addressDialog?.findViewById<Spinner>(R.id.dropdownMenu1)
                            val districtlist = addressDialog?.findViewById<Spinner>(R.id.dropdownMenu2)
                            stateTextView?.visibility = View.VISIBLE
                            districtTextView?.visibility = View.VISIBLE
                            statelist?.visibility = View.INVISIBLE
                            districtlist?.visibility = View.INVISIBLE
                            state = address.adminArea
                            district = address.locality
                            stateTextView?.text = address.adminArea
                            //Toast.makeText(this@add_room, "trying"+ state.toString(), Toast.LENGTH_SHORT).show()
                            districtTextView?.text = address.locality
                            localityEditText?.setText(address.locality)
                            houseNoEditText?.setText(address.getAddressLine(0))
                            pincodeEditText?.setText(address.postalCode)
                            //updateDialogWithLocationDetails()
                           // Toast.makeText(this, "Your Location: $state,$district,$locality,$house_no,$pincode", Toast.LENGTH_LONG).show()
                        }
                    }
                } ?: run {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateDialogWithLocationDetails() {
        // Update TextViews and EditTexts in the dialog with the extracted location details
        val stateTextView = addressDialog?.findViewById<TextView>(R.id.state)
        val districtTextView = addressDialog?.findViewById<TextView>(R.id.District)
        val localityEditText = addressDialog?.findViewById<EditText>(R.id.locality)
        val houseNoEditText = addressDialog?.findViewById<EditText>(R.id.house_no)
        val pincodeEditText = addressDialog?.findViewById<EditText>(R.id.pincode)

        stateTextView?.text = state
        districtTextView?.text = district
        localityEditText?.setText(locality)
        houseNoEditText?.setText(house_no)
        pincodeEditText?.setText(pincode)
    }
    private fun showCustomDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogLayout = inflater.inflate(R.layout.select_address_popup, null)
        val statelist = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu1)
        val districtlist = dialogLayout.findViewById<Spinner>(R.id.dropdownMenu2)
        val locatemeBtn = dialogLayout.findViewById<TextView>(R.id.locateMe_text)
        val stateTextView = dialogLayout.findViewById<TextView>(R.id.state)
        val districtTextView = dialogLayout.findViewById<TextView>(R.id.District)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogLayout)
            .create()
        addressDialog = dialog
        locatemeBtn.setOnClickListener {
//            locationPermissionRequest.launch(arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ))
            onLocateMeClicked()
        }

        val stateDistrictData = Address.getDefaultData()
        val states = stateDistrictData.states
        val districtsMap = stateDistrictData.districtsMap


        val firstSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        firstSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        statelist.adapter = firstSpinnerAdapter

        val mutableOptionsForSecondSpinner = mutableListOf<String>()
        val secondSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableOptionsForSecondSpinner)
        secondSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        districtlist.adapter = secondSpinnerAdapter

        statelist.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                state = states[position]
                val districts = districtsMap[state] ?: emptyList()
                mutableOptionsForSecondSpinner.clear()
                mutableOptionsForSecondSpinner.addAll(districts)
                secondSpinnerAdapter.notifyDataSetChanged()
                districtlist.isEnabled = districts.isNotEmpty()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                districtlist.isEnabled = false
            }

        }


        val pincode1 = dialogLayout.findViewById<EditText>(R.id.pincode)
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
            if(state.isEmpty() || district.isEmpty()){
                state = statelist.selectedItem.toString()
                district = districtlist.selectedItem.toString()
            }
            locality = dialogLayout.findViewById<EditText>(R.id.locality).text.toString()
            pincode = dialogLayout.findViewById<EditText>(R.id.pincode).text.toString()
            house_no = dialogLayout.findViewById<EditText>(R.id.house_no).text.toString()
            Toast.makeText(this@add_room, locality, Toast.LENGTH_SHORT).show()
            if(state.isEmpty() || district.isEmpty() || locality.isEmpty() || pincode.isEmpty() || house_no.isEmpty()){
                Toast.makeText(this@add_room, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }else{
                dialog.dismiss()
                //state and district is not set
                Toast.makeText(this@add_room, "Your address is set $state$district", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
    }

    companion object {
         const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private suspend fun getOwnerDp():String = GlobalScope.async{
//        GlobalScope.launch(Dispatchers.Main){
//            Toast.makeText(this@add_room, "12", Toast.LENGTH_SHORT).show()
//        }
        val docRef = db.collection("users").document(userkey.toString()).get().await()
        var Url = ""
        try {
            docRef.data?.let {
                owner_name = it["name"].toString()
                Url = it["imageuri"].toString()
            }
        } catch(e:Exception) {
            Log.e("descripn", "Error fetching data from Firestore: ${e.message}")
        }
//        GlobalScope.launch(Dispatchers.Main){
//            Toast.makeText(this@add_room, Url, Toast.LENGTH_SHORT).show()
//        }

        return@async Url
    }.await()

    override fun onBackPressed() {
        ShowDraftMessage() // Call the confirmation dialog when the back button is pressed
    }

    private fun ShowDraftMessage() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Want to save this room as Draft?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                showProgressOverlay(true)
                lifecycleScope.launch {
                    Draft()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                showDeleteRoomConfirmationDialog()
            }
            .show()
    }
    private fun showDeleteRoomConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to discard the room?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Delete the room and navigate back
                GlobalScope.launch (IO){
                    val intent = Intent(this@add_room, owner_home_activity::class.java)
                    startActivity(intent)
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showImageSourceOptions() {
        // Define options in an array
        val options = arrayOf("Choose from Gallery", "Take Photo")

        // Create a dialog for options
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Choose from Gallery option selected
                        chooseFromGallery()
                    }
                    1 -> {
                        // Take Photo option selected
                        takePhoto()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun chooseFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        resultLauncherGallery.launch(galleryIntent)
    }
    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(cameraIntent)
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { handleImageBitmap(it) }
        } else {
            Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private val resultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            // Handle the selected image URI accordingly
            uri?.let { handleImageUri(it) }
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleImageBitmap(bitmap: Bitmap) {
        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        val uri = getImageUri(bitmap)
        binding.pic.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
                //binding.progressBar.visibility = View.VISIBLE
                dpuri = uri
                Toast.makeText(this,dpuri.toString()+"121", Toast.LENGTH_SHORT).show()
            }
            //
        }
    }
    private fun handleImageUri(uri: Uri) {
        // Do something with the selected image URI
        // For example, display the selected image in an ImageView
        binding.pic.setImageURI(uri)
        if (uri != null) {
            if (userkey != "") {
                Log.d("jiji","1")
                //binding.progressBar.visibility = View.VISIBLE
                dpuri = uri
                Toast.makeText(this,dpuri.toString()+"121", Toast.LENGTH_SHORT).show()
            }
            //
        }
    }
    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private suspend fun uploadBG() {
        withContext(IO) { // Ensure this runs on IO thread
            try {
                if (this@add_room::dpuri.isInitialized && dpuri != "".toUri()) {
                    //showProgressOverlay(true)
                    // Wait for the upload to complete
                    val reference = storageRef.getReference("images").child(System.currentTimeMillis().toString())
                    reference.putFile(dpuri!!).await() // Await the completion of the upload
                    val downloadUrl = reference.downloadUrl.await()
                    dpUri = downloadUrl.toString() // Await download URL
                    imagesListforFirebaseUris.add(0, dpUri.toUri())
                }else{
                    dpuri = "".toUri()
                    dpUri = ""
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@add_room, "12345$state", Toast.LENGTH_SHORT).show()
                    fun getLateInitOrNull(propertyName: String): String? {
                        return try {
                            val property = this::class.memberProperties.find { it.name == propertyName }
                            property?.let {
                                it.isAccessible = true // Make the property accessible
                                it.getter.call(this) as? String
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val lengthValue = getLateInitOrNull("length")
                    val widthValue = getLateInitOrNull("width")
                    val ownerNameValue = getLateInitOrNull("owner_name")
                    val amountValue = getLateInitOrNull("amount")
                    val briefDescriptionValue = getLateInitOrNull("breif_description")
                    val dpUriValue = getLateInitOrNull("dpUri")
                    val imagesListForFirebaseUrisValue = getLateInitOrNull("imagesListforFirebaseUris")
                    Toast.makeText(this@add_room, "$state 987", Toast.LENGTH_SHORT).show()
                    val addressDraft = hashMapOf(
                        "state" to (state),
                        "district" to (district),
                        "locality" to (locality),
                        "pincode" to (pincode),
                        "house_no" to (house_no)
                    )
                    // TODO trying null is the toast in uploadBG
                    Toast.makeText(this@add_room, "trying1"+ state.toString(), Toast.LENGTH_SHORT).show()
                    val updateDataDraft: Map<String, Any?> = hashMapOf(
                        "length" to (lengthValue ?: ""),
                        "width" to (widthValue ?: ""),
                        "owner_name" to (ownerNameValue ?: ""),
                        "amount" to (amountValue ?: ""),
                        "breif_description" to (briefDescriptionValue ?: ""),
                        "roomId" to roomId,
                        "dpuri" to (dpUriValue ?: ""),
                        "imageuri" to (imagesListforFirebaseUris ?: ""),
                        "ownerId" to ownerId,
                        "ownerDpUrl" to roomOwnerDpUrl,
                        "address" to addressDraft
                    )
                    showProgressOverlay(false)
                    saveRoomDataDraft(updateDataDraft)
                }
            }
        }
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
                        Toast.makeText(this@add_room, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                showProgressOverlay(false)
                Toast.makeText(this@add_room, "All images uploaded successfully", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private suspend fun uploadImage(imageUri: Uri): Uri = GlobalScope.async {
        return@async storageref.child("images/${System.currentTimeMillis()}").putFile(imageUri).await().metadata?.reference?.downloadUrl?.await()
            ?: throw RuntimeException("Failed to upload image")
    }.await()
    private fun UploadTheRoom(){
        showProgressOverlay(true)
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        amount = binding.amount.text.toString()
        breif_description = binding.RoomDescription.text.toString()


        if (!isInputDataValid()) {
            showProgressOverlay(false)
            Toast.makeText(this, "Please provide all the required details for room", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAddressValid()) {
            showProgressOverlay(false)
            Toast.makeText(this, "Please provide all the address details", Toast.LENGTH_SHORT).show()
            return
        }


            CoroutineScope(Dispatchers.Main).launch {
                userkey?.let { key ->
                    if (this@add_room::dpuri.isInitialized && dpuri != "".toUri()) {
                        showProgressOverlay(true)
                        // Wait for the upload to complete
                        uploadBG()
                    }
                }
                val address = hashMapOf(
                    "state" to state,
                    "district" to district,
                    "locality" to locality,
                    "pincode" to pincode,
                    "house_no" to house_no,
                )

                if(dpUri.isNotEmpty()){
                    val updateData = hashMapOf(
                        "length" to length,
                        "width" to width,
                        "owner_name" to owner_name,
                        "amount" to amount,
                        "breif_description" to breif_description,
                        "roomId" to roomId,
                        "dpuri" to dpUri,
                        "imageuri" to imagesListforFirebaseUris,
                        "ownerId" to auth.currentUser?.uid.toString(),
                        "ownerDpUrl" to roomOwnerDpUrl,
                        "address" to address
                    )

                    saveRoomData(updateData)
                }else{
                    Toast.makeText(this@add_room, "Please Select an image first for your room", Toast.LENGTH_SHORT).show()
                }

            }

    }
    private fun saveRoomData(updateData: HashMap<String, Any>) {
        val docref2 = db.collection(ownerId)
        if (docref2 != null) {
            docref2.add(updateData)
                .addOnSuccessListener {
                    val documentId = it.id
                    roomId = documentId
                    val docRefUser = db.collection("Rooms").document(documentId)
                    if (docRefUser != null) {

                        updateData["roomId"] = documentId
                        updateData["status"] = "published"
                        updateData["OwnerUsername"] = userkey.toString()
                        docref2.document(documentId).update(updateData)
                            .addOnSuccessListener {
                                docRefUser.set(updateData)
                                    .addOnSuccessListener {
                                        showProgressOverlay(false)
                                        Toast.makeText(
                                            this@add_room, "Success", Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        backtoHome()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this@add_room,
                                            "failure",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }

                    }
                    Toast.makeText(this@add_room, "Success", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@add_room, "failure", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    

    private fun Draft(){
        length = binding.roomlength.text.toString()
        width = binding.roomwidth.text.toString()
        amount = binding.amount.text.toString()
        breif_description = binding.RoomDescription.text.toString()

        CoroutineScope(Dispatchers.Main).launch {
            userkey?.let { key ->

                uploadBG()
            }

        }
    }
    private fun saveRoomDataDraft(updateData: Map<String, Any?>) {
        showProgressOverlay(true)
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@add_room, "1245", Toast.LENGTH_SHORT).show()
            try {
                val docref2 = db.collection(ownerId)
                if (docref2 != null) {
                    val updatedDataWithStatus = updateData.toMutableMap()
                    updatedDataWithStatus["status"] = "draft"

                    val documentReference = docref2.add(updatedDataWithStatus).await() // Await the addition of the document
                    roomId = documentReference.id
                    updatedDataWithStatus["roomId"] = roomId
                    updatedDataWithStatus["ownerusername"] = userkey

                    // Update the document with roomId and owner username
                    docref2.document(roomId).update(updatedDataWithStatus).await()

                    // Add room ID to the user's draft room list
                    db.collection("users").document(userkey.toString()).update("DraftRoomId", FieldValue.arrayUnion(roomId)).await()

                    Toast.makeText(this@add_room, "Draft saved successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@add_room, "Error saving draft: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("saveRoomDataDraft", "Error: ${e.message}", e)
            } finally {
                showProgressOverlay(false) // Hide the progress overlay in the finally block
                backtoHome() // Navigate back to home activity
            }
        }
    }

    private fun backtoHome(){
        val intent = Intent(
            this@add_room, owner_home_activity::class.java
        )
        startActivity(intent)
        finish()
    }
    override fun onStop() {
        super.onStop()
        //room must get to draft
        // If user exits without saving, delete the temporary room
        GlobalScope.launch(Dispatchers.IO) {
            if (!checkIfAllDetailsFilled()) {
                //userkey?.let { deleteRoom("Rooms", it) }
            }
        }
    }
   

    private suspend fun checkIfAllDetailsFilled(): Boolean {
        val length = binding.roomlength.text.toString()
        val width = binding.roomwidth.text.toString()
        val amount = binding.amount.text.toString()
        val breif_description = binding.RoomDescription.text.toString()
        return !(length.isEmpty() || width.isEmpty() || amount.isEmpty() || breif_description.isEmpty())
    }
    private fun isAddressValid(): Boolean {
        return try {
            if (state.isBlank() || district.isBlank() || locality.isBlank() || pincode.isBlank() || house_no.isBlank()) { // Check if 'state' is initialized and not empty
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
        return length.isNotEmpty() && width.isNotEmpty() &&  amount.isNotEmpty() &&
                owner_name.isNotEmpty() && breif_description.isNotEmpty()
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private fun showProgressOverlay(show: Boolean) {
        //binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isClickable = show
        binding.root.isFocusable = show
        if (show) startAnimation() else stopAnimation()
    }
    private fun startAnimation() {
        touchInterceptor.visibility = View.VISIBLE
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
    }
    private fun stopAnimation() {
        touchInterceptor.visibility = View.INVISIBLE
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.INVISIBLE
    }

}