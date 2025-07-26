package com.example.gpstest.nearby_places

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gpstest.R

class NearbyPlacesAdapter(
    private val context: Context,
    private val list: ArrayList<NearbyPlacesModel>,
    private val itemClickListener: OnItemClick
) : RecyclerView.Adapter<NearbyPlacesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nearby_places_adapter_model, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = list[position]
         holder.placeName.isSelected=true
        holder.img.setBackgroundResource(list.img)
        holder.placeName.text = list.name
        holder.itemView.setOnClickListener {
            holder.itemView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                holder.itemView.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                itemClickListener.onClick(position)
            },200)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.img)
        val placeName = view.findViewById<TextView>(R.id.name_nearby_places)

    }
}