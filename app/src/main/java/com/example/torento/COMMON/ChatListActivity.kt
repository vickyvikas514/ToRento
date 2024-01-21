package com.example.torento.COMMON

import ChatAdapter
import ChatListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.torento.DATACLASS.Message
import com.example.torento.DATACLASS.MessageOwner

import com.example.torento.R
import com.example.torento.databinding.ActivityChatListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChatListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var chatsReference: DatabaseReference
    private lateinit var adapter: ChatListAdapter
    private lateinit var binding: ActivityChatListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return
        val documentid = intent.getStringExtra("documentid")
        val receiverUserId = intent.getStringExtra("userId")
        Toast.makeText(this, receiverUserId, Toast.LENGTH_SHORT).show()
        chatsReference = FirebaseDatabase.getInstance().reference.child(currentUserId)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)

        adapter = ChatListAdapter(currentUserId)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        chatsReference.child(documentid.toString()).orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(MessageOwner::class.java)
                if (chat != null) {
                        if(chat.text==documentid){
                            // Display the message in your UI
                            adapter.addMessage(chat)
                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                        }



                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener{
            override fun onItemClick(receivedId:String) {
                val intent = Intent(this@ChatListActivity,ChatActivity::class.java)
                intent.putExtra("userId",receivedId)
                startActivity(intent)
            }

        })
    }

    }
