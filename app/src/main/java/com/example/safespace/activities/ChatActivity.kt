package com.example.safespace.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.adapters.ChatAdapter
import com.example.safespace.databinding.ActivityChatBinding
import com.example.safespace.models.ChatMessage
import com.example.safespace.models.User
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding
    private lateinit var receiverUser : User
    private lateinit var chatMessages : MutableList<ChatMessage>
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var db : FirebaseFirestore


    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        label@ EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                return@EventListener
            }

            if (value != null) {
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED)
                    {
                        val chatMessage = ChatMessage()
//                      chatMessage.senderId =  documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                        chatMessage.senderId =  documentChange.document.getString(Constants.KEY_USER_ID).toString()
                        chatMessage.recieverId =  documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                        chatMessage.message =  documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                        chatMessage.dateTime =  getDateTime(documentChange.document.getTimestamp(Constants.KEY_TIMESTAMP)?.toDate())
                        chatMessage.dateObject = documentChange.document.getTimestamp(Constants.KEY_TIMESTAMP)!!
//                      Firebase doesn't store DATE type objects -> Error when attempting to get one.
                        chatMessages.add(chatMessage)
                    }
                }
                chatMessages.sortBy { it.dateObject }
                if (chatMessages.size == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size,chatMessages.size);
                    binding.recycleViewChat.smoothScrollToPosition(chatMessages.size-1);
                }
                binding.recycleViewChat.visibility = View.VISIBLE;
            }
            binding.progressBar.visibility = View.GONE
        }

    private fun listenMessages()
    {
        db.collection(Constants.KEY_COLLECTION_CHAT)
           // .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
            .addSnapshotListener(eventListener)
        db.collection(Constants.KEY_COLLECTION_CHAT)
            //.whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
            .whereEqualTo(Constants.KEY_USER_ID,receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadRecieverDetails()
        initializeVariables()
        setListeners()
        listenMessages()
    }


    private fun initializeVariables()
    {
        db = FirebaseFirestore.getInstance()
        chatMessages = mutableListOf()
        preferenceManager= PreferenceManager(this)
        chatAdapter = ChatAdapter(chatMessages,getBitmapFromEncoded(receiverUser.image),preferenceManager.getString(Constants.KEY_USER_ID).toString())
        binding.recycleViewChat.adapter = chatAdapter

    }

    private fun getBitmapFromEncoded(encodedBitmap : String) : Bitmap
    {
        var arr = Base64.decode(encodedBitmap,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(arr,0,arr.size)
    }

    private fun loadRecieverDetails()
    {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.tvName.text = receiverUser.name.toString()
    }

    private fun sendMessage()
    {
        val map : HashMap<String, Any> = HashMap()
//      val time : java.util.Date = Calendar.getInstance().time
        val time = Timestamp.now()


        map[Constants.KEY_USER_ID] = preferenceManager.getString(Constants.KEY_USER_ID).toString()
        map[Constants.KEY_RECEIVER_ID] = receiverUser.id
        map[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        map[Constants.KEY_TIMESTAMP] = time // Possible no need to parse?
        db.collection(Constants.KEY_COLLECTION_CHAT).add(map)
        binding.inputMessage.text = null
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener{
            onBackPressed()
        }

        binding.layoutSend.setOnClickListener{
            sendMessage()
        }
    }




    private fun getDateTime(date : Date?) : String
    {
        return SimpleDateFormat("dd MM yyyy - hh:mm a",Locale.getDefault()).format(date)
    }
}