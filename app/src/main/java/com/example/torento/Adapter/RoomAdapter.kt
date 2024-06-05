package com.example.torento.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.torento.R
import com.example.torento.DATACLASS.Room


class RoomAdapter(val context: Context, var rooms:List<Room>, var idlist:List<String>): Adapter<RoomAdapter.RoomViewHolder>() {

    private lateinit var itemClickListener: OnItemClickListener
    interface OnItemClickListener {
        fun onItemClick(documentId:String,position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
    class RoomViewHolder(itemView: View, listner: OnItemClickListener, docidlist:List<String>):ViewHolder(itemView){
        val RoomImage = itemView.findViewById<ImageView>(R.id.pic)
        val RoomSizeText = itemView.findViewById<TextView>(R.id.textline1)
        val RoomDescripText = itemView.findViewById<TextView>(R.id.textline2)
        val RoomOwnerDpImage = itemView.findViewById<ImageView>(R.id.dp)
        init {
            itemView.setOnClickListener{
                 // Call the onItemClick method of the listener and pass the document ID
                    listner.onItemClick(docidlist[adapterPosition],adapterPosition)
               }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.room_list_item,parent,false)
        return  RoomViewHolder(view, itemClickListener,idlist)
    }

    override fun getItemCount(): Int {
       return rooms.size
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.RoomSizeText.text = room.sizeofroom
        holder.RoomDescripText.text = room.roomdescriptiontext
        Glide.with(context).load(room.roomimageurl).into(holder.RoomImage)
        if(room.roomOwnerDpUrl=="temp" || room.roomOwnerDpUrl==null){
            holder.RoomOwnerDpImage.setImageResource(R.drawable.demodp)
        }else{
            Glide.with(context).load(room.roomOwnerDpUrl).into(holder.RoomOwnerDpImage)
        }


    }



}