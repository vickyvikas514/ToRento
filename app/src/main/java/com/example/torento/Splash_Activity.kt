package com.example.torento

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.torento.LOGIN.LandingPage
import com.example.torento.OWNER.owner_home_activity
import com.example.torento.USER.user_home_activity
import com.example.torento.databinding.ActivitySplashBinding

class Splash_Activity : AppCompatActivity() {
    val SHARED_PREF:String = "sharedPrefs"
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.SplashTheme)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.logo
        binding.videoView.setVideoURI(Uri.parse(videoPath))
        binding.videoView.start()
        binding.videoView.setOnCompletionListener {
            Handler(Looper.getMainLooper()).postDelayed({
                // Check if the user is logged in using SharedPreferences

                val check = isLoggedIn()
                if (check=="user") {
                    // Navigate to HomeActivity
                    startActivity(Intent(this@Splash_Activity, user_home_activity::class.java))
                } else if(check=="owner") {
                    // Navigate to LoginActivity
                    startActivity(Intent(this@Splash_Activity, owner_home_activity::class.java))
                }else{
                    startActivity(Intent(this@Splash_Activity, LandingPage::class.java))
                }
                // Finish the current activity
                finish()
            }, SPLASH_DELAY)
        }

    }
    private fun isLoggedIn(): String {
        // Use SharedPreferences to check login status
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val check:String? = sharedPreferences.getString("name" , "")
        val checkusertype: String? = sharedPreferences.getString("usertype","")
        if(check.equals("true") && checkusertype.equals("tenant")){
           return "user"
        }else if(check.equals("true") && checkusertype.equals("owner")){
           return "owner"
        }
        return "not_logged_in"
    }

    companion object {
        private const val SPLASH_DELAY = 1000L // 1 seconds
    }
}