package com.example.torento.DATACLASS

data class Room(val sizeofroom:String,val roomdescriptiontext:String,
    val roomimageurl:String, val roomOwnerDpUrl : String)

data class Message(val senderId: String, val receiverId: String, val text: String, val timestamp: Long) {
    constructor() : this("","", "", 0)
}
data class Chat(
    val chatId: String = "", // Unique identifier for the chat
    val participants: Map<String, Boolean> = emptyMap(), // Map of participant user IDs
    val messages: Map<String, Message> = emptyMap() // Map of messages in the chat
)
data class MessageOwner(val senderId: String, val receiverId: String, val text: String, val name:String,val timestamp: Long) {
    constructor() : this("","", "","", 0)
}

