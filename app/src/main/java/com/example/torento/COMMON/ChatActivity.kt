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
import com.example.torento.DATACLASS.MessageOwner
import com.example.torento.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var messagesReferenceOwner: DatabaseReference
    private lateinit var messagesReference: DatabaseReference
    private lateinit var adapter: ChatAdapter
    private var db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

       // Toast.makeText(this@ChatActivity,temp , Toast.LENGTH_SHORT).show()
        auth = FirebaseAuth.getInstance()
        val receiverUserId = intent.getStringExtra("userId") // Replace with the actual user ID of the other person
        val senderId = "0D2bMnHrhcWCSkyRlklWMhY0NTS2"
        val documentId =  intent.getStringExtra("documentid")
        val username = intent.getStringExtra("username")
        //Toast.makeText(this, documentId, Toast.LENGTH_SHORT).show()
         messagesReference = FirebaseDatabase.getInstance().reference.child("messages")
        messagesReferenceOwner = FirebaseDatabase.getInstance().reference.child(receiverUserId.toString())




        addFirebaseConnectionListener()//to check the connection of firebase

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)

        adapter = ChatAdapter(senderId)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        var name=""
        GlobalScope.launch(Dispatchers.IO) {
             name = username?.let { getname(it) }.toString()
        }
        val currentUserId = auth.currentUser?.uid ?: return
        messagesReference.orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    if (((message.senderId == currentUserId) && (message.receiverId == receiverUserId)) ||
                        ((message.senderId == receiverUserId) && (message.receiverId == currentUserId))
                    ) {
                        // Display the message in your UI
                        adapter.addMessage(message)
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }

                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
        getsize { size ->
            if (size != -1) {
                // Use the size here
                Log.d("Firebase", "Size of messages collection: $size")
            } else {
                // Handle the error
                Log.e("Firebase", "Failed to get the size of messages collection")
            }
        }
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (receiverUserId != null) {
                    getsize { size->
                        if(size==0){
                            sendMessage(receiverUserId,messageText)
                            sendMessageOwner(receiverUserId,documentId,name)
                        }else{
                            sendMessage(receiverUserId,messageText)
                        }
                    }

                }
                messageEditText.text.clear()
            }
        }


    }
     fun getsize(callback: (Int) -> Unit){

        val databaseReference = FirebaseDatabase.getInstance().getReference("messages")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // The 'snapshot' contains the data at the 'messages' node

                // Get the size of the collection
                val collectionSize = snapshot.childrenCount.toInt()

                callback(collectionSize)
                // Now 'collectionSize' contains the number of items in the 'messages' node
                // You can use this value as needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e("Firebase", "Error reading messages: ${error.message}")
                callback(-1)
            }
        })
    }
    suspend fun getname(username:String): String = GlobalScope.async{

        var name:String = ""
        try {
            val docref = db.collection("users").document(username).get().await()
            if (docref != null) {
                docref.data?.let {
                    name = it["name"].toString()
                }
            }

            else {
                Toast.makeText(this@ChatActivity, "DocRef is NULL", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception){
            Log.e("Profile", "Error fetching data from Firestore: ${e.message}")
        }
        return@async name
    }.await()
        private fun sendMessageOwner(receiverId: String, documentId: String?,name:String){
            val senderId = auth.currentUser?.uid ?: return
            val messageOwner =

                documentId?.let { MessageOwner(senderId,receiverId, it,name,System.currentTimeMillis()) }
            messagesReferenceOwner.child(documentId.toString()).push().setValue(messageOwner)
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

    private fun sendMessage(receiverId: String,messageText: String) {
        Log.d("CHAT", "Sending message: $messageText")
        val senderId = auth.currentUser?.uid ?: return
        Log.d("CHAT", "Current user ID: $senderId")
       val message = Message(senderId,receiverId,messageText,System.currentTimeMillis())
        Log.d("CHAT", "Message created: $message")
        messagesReference.push().setValue(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "4", Toast.LENGTH_SHORT).show()
                    // Message sent successfully
                    Log.d("CHAT","Message sent successfully")
                } else {
                    // Handle the error
                    Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }

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
}