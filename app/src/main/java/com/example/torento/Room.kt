package com.example.torento

data class Room(val sizeofroom:String,val roomdescriptiontext:String,
    val roomimageurl:String)
object RoomDataSource{
    val rooms:List<Room> = listOf(
        Room(
            "Tzeitel",
            "7",
            "https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1698574420926?alt=media&token=2eb1a169-cd2b-43c4-82b7-54681c945c9b"
        ),
        Room(
            "Tzeitel",
            "7",
            "https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1698574420926?alt=media&token=2eb1a169-cd2b-43c4-82b7-54681c945c9b"
        ),
        Room(
            "Tzeitel",
            "7",
            "https://firebasestorage.googleapis.com/v0/b/torento-865ac.appspot.com/o/images%2F1698574420926?alt=media&token=2eb1a169-cd2b-43c4-82b7-54681c945c9b"
        ),
    )

}
