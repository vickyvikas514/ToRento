package com.example.torento

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PicsAdapter(val context: Context, private var pics: List<String>): RecyclerView.Adapter<PicsAdapter.PicViewHolder>() {
    class PicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val PicImage = itemView.findViewById<ImageView>(R.id.pic_image)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.dcrn_card,parent,false)
        return PicViewHolder(view)
    }
    override fun onBindViewHolder(holder: PicViewHolder, position: Int) {
        val pic = pics[position]
        Toast.makeText(context, pic, Toast.LENGTH_SHORT).show()
        Glide.with(context).load(pic).into(holder.PicImage)
        holder.itemView.setOnClickListener{
            Toast.makeText(context, pic, Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return pics.size
    }
}