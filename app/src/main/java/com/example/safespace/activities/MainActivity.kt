package com.example.safespace.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safespace.adapters.RecentEventAdapter
import com.example.safespace.bluetooth.BLEService
import com.example.safespace.databinding.ActivityMainBinding
import com.example.safespace.models.RecentEvent
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.collect.ImmutableList
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception

class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager : PreferenceManager
    private var bleService = BLEService()

    companion object
    {
        lateinit var userName : String
    }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        FirebaseFirestore.setLoggingEnabled(true)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseMessaging.getInstance().subscribeToTopic("helper");
        preferenceManager = PreferenceManager(this)
        setListeners()
        updateUI()
        getToken()
    }

    @SuppressLint("SetTextI18n")
    private fun setListeners()
    {
        binding.imageLogOut.setOnClickListener { signOut() }
        binding.fabNewChat.setOnClickListener{ startActivity(Intent(this,UserListActivity::class.java))}
        binding.mapButton.setOnClickListener {
            val intent = Intent(this,MapActivity::class.java)
            startActivity(intent)
        }
        binding.connectTv.setOnClickListener {
            startService(Intent(this,BLEService::class.java))
            binding.connectText.text = "Connected"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this,BLEService::class.java))
    }
    private fun raiseToast(string : String)
    {
        Toast.makeText(this,string, Toast.LENGTH_SHORT).show()
    }

    private fun getToken()
    {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            this.updateToken(it)
        }
    }

    private fun updateToken(token : String)
    {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID).toString()
        )
        documentReference.update(Constants.KEY_FIRESTORECLOUDMESSAGING_TOKEN,token)
            .addOnFailureListener{
                raiseToast("Failed to update token, exception: "+it.message)
            }
    }

    private fun updateUI()
    {
        binding.tvName.text = preferenceManager.getString(Constants.KEY_NAME)
        val imageByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.size)
        binding.imageProfile.setImageBitmap(imageBitmap)
        userName = preferenceManager.getString(Constants.KEY_NAME) as String
        updateRecentEventRV()



    }
    private fun updateRecentEventRV() {
        //// UPDATE RECYCLEVIEW ////
        var recentEventData : MutableList<RecentEvent> = mutableListOf()
        // get all events, build list

        val db = FirebaseFirestore.getInstance()
        Log.d("updateRecentEvent", "before complete listener")
        db.collection(Constants.KEY_LOCATIONS).get().addOnCompleteListener {
            var tempArray : MutableList<RecentEvent> = mutableListOf()
            Log.d("updateRecentEvent", "addOnCompleteListener")
            if (it.isSuccessful && it.result != null) {
                Log.d("updateRecentEvent", "in if")
                var userName: String
                var timeHour: String
                var timeMinute: String
                var dateDay: String
                var dateMonth: String
                var timeMap: HashMap<String, Any>
                var time: String
                for (document in it.result) {
                    timeMap = document.get("time") as HashMap<String, Any>
                    timeMinute = timeMap["minute"].toString()
                    timeHour = timeMap["hour"].toString()
                    dateDay = timeMap["dayOfMonth"].toString()
                    dateMonth = timeMap["monthValue"].toString()
                    time = "$timeHour:$timeMinute"
                    userName = document.get("user") as String
                    Log.d("updateRecentEvent", "userName: $userName")
                    var recentEvent: RecentEvent = RecentEvent()
                    recentEvent.name = userName
                    recentEvent.dateAndTime = "$time, $dateDay/$dateMonth"
//
//                    db.collection("users").get().addOnCompleteListener { it2 ->
//                        Log.d("isSuccessful", "${it2.isSuccessful}")
//                        Log.d("result", "${it2.result}")
//                        Log.d("getImage", "addOnCompleteListener")
//                        if (it2.isSuccessful && it2.result != null) {
//                            Log.d("getImage", "in if")
//                            for (doc in it2.result) {
//                                if (userName == doc.getString("name")) {
//                                    recentEvent.image = doc.getString("image") as String
//                                }
//                            }
//                        }
//
////                      tempArray+= recentEvent
//                        Log.d("recentEventData size", recentEventData.size.toString())
//                    }
                    tempArray+= recentEvent
                    Log.d("recentEventData here", recentEventData.size.toString())
                    Log.d("recentEvent name", "${recentEvent.name}")
                    Log.d("recentEvent date", "${recentEvent.dateAndTime}")
//                    Log.d("recentEvent image","${recentEvent.image}")
                    recentEventData = tempArray
                }
                Log.d("recentEventData final", recentEventData.size.toString())
                val adapter: RecentEventAdapter = RecentEventAdapter(recentEventData)
                binding.recentEventRecyclerView.adapter = adapter
                binding.recentEventRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.recentEventRecyclerView.visibility = View.VISIBLE
            }
        }
    }

        private fun getImage(userName: String): String
        {
            Log.d("getImage", "start")
            val db = FirebaseFirestore.getInstance()
            lateinit var image: String
            db.collection("users").get().addOnCompleteListener {
                Log.d("isSuccessful", "${it.isSuccessful}")
                Log.d("result", "${it.result}")
                Log.d("getImage", "addOnCompleteListener")
                if (it.isSuccessful && it.result != null) {
                    Log.d("getImage", "in if")
                    for (doc in it.result) {
                        if (userName == doc.getString("name")) {
                            image = doc.getString("image") as String
                        }
                    }

                }
                image = "test"
            }.addOnFailureListener {
                Log.d("Failure in getImage", it.toString())
            }


            return image
        }

    private fun signOut()
    {
        raiseToast("Signing out")
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID).toString())
        val updates : HashMap<String, Any> = HashMap()
        updates[Constants.KEY_FIRESTORECLOUDMESSAGING_TOKEN] = FieldValue.delete()
        docRef.update(updates).addOnSuccessListener {
            preferenceManager.clear()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }.addOnFailureListener{
            raiseToast("Couldn't sign out. Exception: "+it.message)
            }
         }
    }
