package com.example.torento.LOGIN

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.torento.databinding.ActivityLandingPageBinding
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.USER.user_home_activity


class LandingPage : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding

    val SHARED_PREF:String = "sharedPrefs"
    companion object{
        lateinit var usertype:String
        var userid = "temp"
        var num=0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val check:String? = sharedPreferences.getString("name" , "")
        val checkusertype: String? = sharedPreferences.getString("usertype","")
        if(check.equals("true") && checkusertype.equals("tenant")){
            val intent = Intent(this, user_home_activity::class.java)
            startActivity(intent)
            finish()
        }else if(check.equals("true") && checkusertype.equals("owner")){
            val intent = Intent(this, owner_home_activity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivityLandingPageBinding.inflate(
            layoutInflater)
        setContentView(binding.root)
        binding.landlord.setOnClickListener{
            usertype ="owner"
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
        binding.tenant.setOnClickListener{
            usertype = "tenant"
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
    }
}