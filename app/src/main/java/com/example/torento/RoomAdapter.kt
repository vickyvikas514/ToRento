package com.example.torento

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide

class RoomAdapter(val context: Context,val rooms:List<Room>): Adapter<RoomAdapter.RoomViewHolder>() {



    class RoomViewHolder(itemView: View):ViewHolder(itemView){
        val RoomImage = itemView.findViewById<ImageView>(R.id.pic)
        val RoomSizeText = itemView.findViewById<TextView>(R.id.textline1)
        val RoomDescripText = itemView.findViewById<TextView>(R.id.textline2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.room_list_item,parent,false)
        return RoomViewHolder(view)
    }

    override fun getItemCount(): Int {
       return rooms.size
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {

        val room = rooms[position]
        holder.RoomSizeText.text = room.sizeofroom
        holder.RoomDescripText.text = room.roomdescriptiontext
        Glide.with(context).load(room.roomimageurl).into(holder.RoomImage)
       //item onClick
        holder.itemView.setOnClickListener{
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        }
    }

}