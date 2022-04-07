package com.example.safespace.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.databinding.ActivitySignInBinding
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners()
    {
        binding.tvCreateNewAccount.setOnClickListener{
                val newAccountIntent = Intent(this,SignUpActivity::class.java)
                startActivity(newAccountIntent)
        }

        binding.buttonSignIn.setOnClickListener{
            if (checkLoginDetails()) signIn()
        }
    }

    private fun isLoading(loading : Boolean) = if (loading)
    {
        binding.buttonSignIn.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }
    else
    {
        binding.progressBar.visibility = View.INVISIBLE
        binding.buttonSignIn.visibility = View.VISIBLE

    }

    private fun signIn()
    {
        isLoading(true)
        val db : FirebaseFirestore  = FirebaseFirestore.getInstance()
        db.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL,binding.emailAddress.text.toString().trim())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.password.text.toString().trim()).get().addOnCompleteListener{
                if(it.isSuccessful() && it.getResult()!=null && it.getResult().documents.size > 0) {
                        val snapShot: DocumentSnapshot = it.getResult().documents.get(0)
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID, snapShot.getId())
                        preferenceManager.putString(Constants.KEY_NAME, snapShot.getString(Constants.KEY_NAME).toString())
                        preferenceManager.putString(Constants.KEY_IMAGE, snapShot.getString(Constants.KEY_IMAGE).toString())
                        val intent = Intent(this,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                else {
                    isLoading(false)
                    raiseToast("Sign in error.")
                }
            }

    }

    private fun raiseToast(string : String)
    {
        Toast.makeText(this,string, Toast.LENGTH_SHORT).show()
    }

    private fun checkLoginDetails() : Boolean
    {
        if (binding.emailAddress.text.toString().trim().isEmpty())
        {
            raiseToast("Please enter email address.")
            return false
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.emailAddress.text.toString().trim()).matches())
        {
            raiseToast("Please enter valid email address")
            return false
        }
        else {
            if(binding.password.text.toString().trim().isEmpty()) {
                raiseToast("Please enter password")
                return false
            } else return true
        }
    }

//   ########## TEST DATABASE ENTRY ##########
//
//    private fun addToDatabase()
//    {
//        val db = FirebaseFirestore.getInstance()
//        val map : HashMap<String, Any> = HashMap()
//        map.put("first_name","Yossi")
//        map.put("last_name","Meiri")
//
//        db.collection("Users")
//            .add(map)
//            .addOnSuccessListener {
//                Toast.makeText(this,"Data inserted",Toast.LENGTH_SHORT).show() }
//            .addOnFailureListener {
//                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
//            }
//
//    }

}