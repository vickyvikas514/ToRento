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
import com.google.firebase.database.ValueEventListener

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
        chatsReference = FirebaseDatabase.getInstance().reference.child(currentUserId).child(documentid.toString())
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)

        adapter = ChatListAdapter(currentUserId)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter


        chatsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val memberId = snapshot.key
                if (memberId != null) {
                    val memberReference = chatsReference.child(memberId)
                    memberReference.orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                            val message = snapshot.getValue(MessageOwner::class.java)
                            if (message != null) {
                                if(message.text==documentid) {
                                    // Display the message in your UI
                                    adapter.addMessage(message)
                                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                                }
                            }else{
                                Toast.makeText(this@ChatListActivity, "fail", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                        override fun onChildRemoved(snapshot: DataSnapshot) {}

                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle child changed event
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle child removed event
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle child moved event
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })



        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener{
            override fun onItemClick(receivedId:String) {
                val intent = Intent(this@ChatListActivity,ChatActivity::class.java)
                intent.putExtra("userId",receivedId)
                intent.putExtra("documentid",documentid)
                intent.putExtra("usertype","owner")
                startActivity(intent)
            }

        })
    }

    }
