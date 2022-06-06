package com.example.safespace.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.bluetooth.BLEService
import com.example.safespace.databinding.ActivityMainBinding
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager : PreferenceManager
    private var bleService = BLEService()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        FirebaseFirestore.setLoggingEnabled(true)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
        updateUI()
        getToken()
    }

    private fun setListeners()
    {
        binding.imageLogOut.setOnClickListener { signOut()}
        binding.fabNewChat.setOnClickListener{ startActivity(Intent(this,UserListActivity::class.java))}
        binding.mapButton.setOnClickListener {
            val intent = Intent(this,MapActivity::class.java)
            startActivity(intent)
        }
        binding.connectTv.setOnClickListener {
            startService(Intent(this,BLEService::class.java))
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