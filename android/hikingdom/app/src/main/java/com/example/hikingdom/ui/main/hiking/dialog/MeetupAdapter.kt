package com.example.hikingdom.ui.main.hiking.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hikingdom.R
import com.example.hikingdom.data.remote.hiking.Meetup
import com.example.hikingdom.data.remote.hiking.Mountain
import com.google.gson.annotations.SerializedName

class MeetupAdapter(val context: Context, val dataset:List<Meetup>): RecyclerView.Adapter<MeetupAdapter.ViewHolder>(){

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val meetupName = view.findViewById<TextView>(R.id.meetup_name)
        val mountainName = view.findViewById<TextView>(R.id.mountain_name)
        val totalMember = view.findViewById<TextView>(R.id.total_member)
        val startAt = view.findViewById<TextView>(R.id.start_at)

        fun bind(meetup: Meetup){
            meetupName.text = meetup.meetupName
            mountainName.text = meetup.mountainName
            totalMember.text = meetup.totalMember.toString()
            startAt.text = meetup.startAt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetupAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_select_meetup, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}