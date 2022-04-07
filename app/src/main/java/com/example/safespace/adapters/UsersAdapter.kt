package com.example.safespace.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Layout
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.safespace.R
import com.example.safespace.databinding.IcUserBinding
import com.example.safespace.listeners.UserListener
import com.example.safespace.models.User

class UsersAdapter (private val users : MutableList<User>, val userListener : UserListener) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>()
{


//    Must call class as inner to get outer class variables
    inner class UsersViewHolder(icUserBinding: IcUserBinding) : RecyclerView.ViewHolder(icUserBinding.root){

        var binding : IcUserBinding = icUserBinding

        fun setUserData(user : User)
        {
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener { userListener.onUserClicked(user) }
        }

        private fun getUserImage(encodedImage : String): Bitmap
        {
            var imgArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imgArray,0,imgArray.size)
        }
    }





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding : IcUserBinding = IcUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
      holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }
}