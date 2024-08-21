package com.example.torento.COMMON

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.torento.R
import com.example.torento.databinding.ActivityFullScreenDpviewBinding

private lateinit var binding:ActivityFullScreenDpviewBinding
class FullScreenDPView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFullScreenDpviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fullScreenImageView: ImageView = findViewById(R.id.fullScreenImageView)

        // Get the image URI passed from the previous activity
        val imageUri = intent.getStringExtra("imageUri")
        Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show()

        // Display the image in full-screen
        if (imageUri != "") {
            Glide.with(this)
                .load(imageUri)
                .into(binding.fullScreenImageView)
        }

        // Set an onClickListener to close the full-screen view when the image is clicked
        fullScreenImageView.setOnClickListener {
            finish() // Close the activity and go back
        }
    }
}