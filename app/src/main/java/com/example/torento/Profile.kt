package com.example.torento

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.TextView
import android.widget.Toast


import com.example.torento.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {
  private lateinit var binding: ActivityProfileBinding
  private lateinit var fauth: FirebaseAuth
  private lateinit var namei:TextView
  private var  db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fauth= FirebaseAuth.getInstance()
        namei = findViewById(R.id.name)
        super.onCreate(savedInstanceState)

        binding.back.setOnClickListener{
            val intent = Intent(this,owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }

        set()
    }
    private fun set(){
        val id  = LandingPage.id
        if(LandingPage.character==1){
            val docref = db.collection("owners").document(id)
            if (docref != null) {
                docref.get().addOnSuccessListener {
                    if(it!=null){
                        val name = it.data?.get("name")?.toString()
                        if(name==null){
                            Toast.makeText( this,"FAIL", Toast.LENGTH_SHORT).show()
                        }else{
                            namei.text = name
                        }

                        binding.username.text  = it.data?.get("username")?.toString()

                        binding.phone.text = it.data?.get("phone")?.toString()

                        binding.email.text = it.data?.get("email")?.toString()

                    }else{
                        Toast.makeText( this,"Fail!!", Toast.LENGTH_SHORT).show()
                    }
                }
                    .addOnFailureListener {
                        Toast.makeText(this,"Failed!!",Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText( this,"VIKAS CHAUDHARY", Toast.LENGTH_SHORT).show()
            }
        }else{
            val docref = db.collection("users").document(id)
            if (docref != null) {
                docref.get().addOnSuccessListener {
                    if(it!=null){
                        val name = it.data?.get("name")?.toString()
                        if(name==null){
                            Toast.makeText( this,"FAIL", Toast.LENGTH_SHORT).show()
                        }else{
                            namei.text = name
                        }

                        binding.username.text  = it.data?.get("username")?.toString()

                        binding.phone.text = it.data?.get("phone")?.toString()

                        binding.email.text = it.data?.get("email")?.toString()

                    }else{
                        Toast.makeText( this,"Fail!!", Toast.LENGTH_SHORT).show()
                    }
                }
                    .addOnFailureListener {
                        Toast.makeText(this,"Failed!!",Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText( this,"VIKAS CHAUDHARY", Toast.LENGTH_SHORT).show()
            }
        }


    }
}