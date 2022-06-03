package com.example.safespace.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.safespace.databinding.IcRecievedMessageBinding
import com.example.safespace.databinding.IcSentMessageBinding
import com.example.safespace.models.ChatMessage

class ChatAdapter (var chatMessages: MutableList<ChatMessage>,
                   var recieverProfileImage: Bitmap,
                   var senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var VIEW_TYPE_SENT = 1
    var VIEW_TYPE_RECIEVED = 2

     inner class SentMessageViewHolder(var icSentMessageBinding: IcSentMessageBinding) : RecyclerView.ViewHolder(icSentMessageBinding.root)
    {
        private var binding = icSentMessageBinding
        fun setData(chatMessage: ChatMessage)
        {
            binding.textMessage.text = chatMessage.message.toString()
            binding.textDateTime.text = chatMessage.dateTime.toString()
        }
    }

    inner class ReceivedMessageViewHolder(icReceivedMessageBinding: IcRecievedMessageBinding) : RecyclerView.ViewHolder(icReceivedMessageBinding.root)
    {
        private var binding : IcRecievedMessageBinding = icReceivedMessageBinding
        fun setData(chatMessage: ChatMessage, receiverProfileImage : Bitmap)
        {
            binding.textMessage.text = chatMessage.message.toString()
            binding.tvDateTime.text = chatMessage.dateTime.toString()
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("ChatAdapter", "in create viewHolder")
        return if (viewType==VIEW_TYPE_SENT) {
            SentMessageViewHolder(IcSentMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        } else {
            ReceivedMessageViewHolder(IcRecievedMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       if (getItemViewType(position)== VIEW_TYPE_SENT)
           (holder as SentMessageViewHolder).setData(chatMessages[position])
        else
           (holder as ReceivedMessageViewHolder).setData(chatMessages[position],recieverProfileImage)
    }

    override fun getItemCount(): Int { return chatMessages.size }

    override fun getItemViewType(position: Int): Int
    {
        return if (chatMessages[position].senderId == senderId) VIEW_TYPE_SENT else VIEW_TYPE_RECIEVED
    }
}




//
//class UsersAdapter (private val users : MutableList<User>, val userListener : UserListener) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>()
//{
//
//
////    Must call class as inner to get outer class variables
//    inner class UsersViewHolder(icUserBinding: IcUserBinding) : RecyclerView.ViewHolder(icUserBinding.root){
//
//        var binding : IcUserBinding = icUserBinding
//
//        fun setUserData(user : User)
//        {
//            binding.tvName.text = user.name
//            binding.tvEmail.text = user.email
//            binding.imageProfile.setImageBitmap(getUserImage(user.image))
//            binding.root.setOnClickListener { userListener.onUserClicked(user) }
//        }
//
//        private fun getUserImage(encodedImage : String): Bitmap
//        {
//            var imgArray = Base64.decode(encodedImage, Base64.DEFAULT)
//            return BitmapFactory.decodeByteArray(imgArray,0,imgArray.size)
//        }
//    }
//
//
//
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
//        val binding : IcUserBinding = IcUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//        return UsersViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
//      holder.setUserData(users[position])
//    }
//
//    override fun getItemCount(): Int {
//        return users.size
//    }
//}