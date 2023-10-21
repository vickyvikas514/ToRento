package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.torento.databinding.ActivityLandingPageBinding


class LandingPage : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding
    companion object{
        var character : Int = 0
        var id:String = ""
    }
    val SHARED_PREF:String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val check:String? = sharedPreferences.getString("name" , "")
        if(check.equals("true")){
            val intent = Intent(this, owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivityLandingPageBinding.inflate(
            layoutInflater)
        setContentView(binding.root)
        binding.landlord.setOnClickListener{
            character=1
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
            finish()
        }
        binding.tenant.setOnClickListener{
            character=2
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }
}