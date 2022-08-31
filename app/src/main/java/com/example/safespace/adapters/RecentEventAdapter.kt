package com.example.safespace.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safespace.R
import com.example.safespace.models.RecentEvent
import com.example.safespace.utilities.Constants

    class RecentEventAdapter(private val mList: List<RecentEvent>) : RecyclerView.Adapter<RecentEventAdapter.ViewHolder>() {

        // create new views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // inflates the card_view_design view
            // that is used to hold list item
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_event_cardview, parent, false)
            return ViewHolder(view)
        }

        // binds the list items to a view
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val ItemsViewModel = mList[position]

            Log.d("bindViewHolderName", "${ItemsViewModel.name}")
            Log.d("bindViewHolderTime", "${ItemsViewModel.dateAndTime}")
//            Log.d("bindViewHolderImage","${ItemsViewModel.image.toString()}")
//            // sets the image to the imageview from our itemHolder class
//            val imageByteArray = Base64.decode(ItemsViewModel.image, Base64.DEFAULT)
//            val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.size)
//            holder.imageView.setImageBitmap(imageBitmap)
            // sets the text to the textview from our itemHolder class
            holder.nameTextView.text = ItemsViewModel.name
            holder.timeTextView.text = ItemsViewModel.dateAndTime

        }

        // return the number of the items in the list
        override fun getItemCount(): Int {
            return mList.size
        }

        // Holds the views for adding it to image and text
        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
//            val imageView: ImageView = itemView.findViewById(R.id.imageview)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val timeTextView : TextView = itemView.findViewById(R.id.timeTextView)
        }
    }
