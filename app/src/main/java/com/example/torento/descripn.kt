package com.example.torento

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torento.databinding.ActivityDescripnBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class descripn : AppCompatActivity() {
    private lateinit var binding: ActivityDescripnBinding
    private var db = Firebase.firestore
    private var imagearraylist :List<String> = listOf("https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1703841827209?alt=media&token=ed6568b7-655f-4077-b194-6f5db7a76611","https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1703841827209?alt=media&token=ed6568b7-655f-4077-b194-6f5db7a76611")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescripnBinding.inflate(layoutInflater)
        setContentView(binding.root)

    //fetchDataFromFirestore("uuu6")
        val Id = intent.getStringExtra("documentid").toString()
        db.collection("Rooms").document(Id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                Toast.makeText(this, "hello ji p1", Toast.LENGTH_SHORT).show()
                if (documentSnapshot.exists()){
                    Toast.makeText(this, "hello ji p9", Toast.LENGTH_SHORT).show()
                    val imageUriList = documentSnapshot.get("imageuri") as? List<String> ?: emptyList()
                    ///imageurilist se nhi ho paa raha hai
                    imageUriList?.forEach { stringValue ->
                        // Access each string value here

                    }

                   // val newAdapter = PicsAdapter(this, imagearraylist)
                    // Set the new adapter to the RecyclerView
                    binding.listPhoto.adapter = PicsAdapter(this,imageUriList)

                    // Notify the adapter that the data set has changed

                    if(imageUriList[0]=="https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1703841827209?alt=media&token=ed6568b7-655f-4077-b194-6f5db7a76611"){
                        Toast.makeText(this, "hello ji paagal", Toast.LENGTH_SHORT).show()
                    }


                } else {
                    Toast.makeText(this, "hello ji p2", Toast.LENGTH_LONG).show()
                    // Document does not exist
                    Log.e("TAG", "Document $ does not exist.")
                }
            }
        binding.listPhoto.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)


        if(imagearraylist[0]=="https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1703841827209?alt=media&token=ed6568b7-655f-4077-b194-6f5db7a76611") {
            Toast.makeText(this, "hii 1", Toast.LENGTH_SHORT).show()
        }
           }



}