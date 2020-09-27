package com.corp.luqman.gettrackcoordinate.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.corp.luqman.gettrackcoordinate.R
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate
import kotlinx.android.synthetic.main.item_data_lokasi.view.*

class LocationAdapter(val listLocation: MutableList<Coordinate>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun setData(coordinate: Coordinate){
            itemView.tv_latitude.text = coordinate.latitude
            itemView.tv_longitude.text = coordinate.longitude
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_data_lokasi, parent, false)

                return ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return listLocation.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locationSelected = listLocation[position]
        holder.setData(locationSelected)
    }
}