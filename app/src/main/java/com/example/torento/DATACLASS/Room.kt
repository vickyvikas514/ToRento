package com.example.torento.DATACLASS

data class Room(val sizeofroom:String,val roomdescriptiontext:String,
    val roomimageurl:String)
data class Message(val userId: String, val text: String, val timestamp: Long) {
    constructor() : this("", "", 0)
}

