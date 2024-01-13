package com.example.torento.COMMON

import ChatAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torento.DATACLASS.Message
import com.example.torento.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("messages")
        addFirebaseConnectionListener()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter()
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    adapter.addMessage(message)
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun addFirebaseConnectionListener() {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    // The app is connected to the Firebase Realtime Database
                    Log.d("FirebaseConnection", "Connected to Firebase")
                } else {
                    // The app is not connected to the Firebase Realtime Database
                    Log.d("FirebaseConnection", "Not connected to Firebase")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseConnection", "Listener was cancelled")
            }
        })
    }
    private fun sendMessage(messageText: String) {
        Log.d("CHAT", "Sending message: $messageText")
        val userId = auth.currentUser?.uid ?: return
        Log.d("CHAT", "Current user ID: $userId")
       val message = Message(userId,messageText,System.currentTimeMillis())
        Log.d("CHAT", "Message created: $message")
        database.push().setValue(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "3", Toast.LENGTH_SHORT).show()
                    // Message sent successfully
                    Log.d("CHAT","Message sent successfully")
                } else {
                    // Handle the error
                    Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }

    }
}