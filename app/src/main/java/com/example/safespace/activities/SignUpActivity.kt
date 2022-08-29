package com.example.safespace.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.databinding.ActivitySignUpBinding
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private var encodedImageString : String = "empty"
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // Get viewbinding variable
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        preferenceManager = PreferenceManager(this)
    }

    private fun setListeners() {
        binding.tvSignIn.setOnClickListener{ this.onBackPressed() }

        binding.buttonSignUp.setOnClickListener {
            Log.d("Is valid?", "Before check")
            if(isValidSignUp())
            {
                Log.d("IS VALID!", "SUCCESS!")
                newSignUp()
            }
        }

        binding.layoutImage.setOnClickListener{
            val intent : Intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun raiseToast(string : String)
    {
        Toast.makeText(this,string,Toast.LENGTH_SHORT).show()
    }

    private fun newSignUp()
    {
        Log.e("newSignUp","Arrived")
        isLoading(true)
        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        val userHashMap : HashMap<String,Any> = HashMap()
        userHashMap[Constants.KEY_NAME] = binding.inputName.text.trim().toString()
        userHashMap[Constants.KEY_EMAIL] = binding.inputEmail.text.trim().toString()
        userHashMap[Constants.KEY_PASSWORD] = binding.inputPassword.text.trim().toString()
        userHashMap[Constants.KEY_IMAGE] = encodedImageString.toString()

        database.collection(Constants.KEY_COLLECTION_USERS).add(userHashMap)
            .addOnSuccessListener {
                isLoading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                preferenceManager.putString(Constants.KEY_USER_ID,it.id)
                preferenceManager.putString(Constants.KEY_IMAGE,encodedImageString)
                val intent : Intent = Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
            isLoading(false)
                it.message?.let { it1 -> raiseToast(it1) }
            }

    }

    private fun encodedImage(bitmap : Bitmap) : String
    {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        if (it.resultCode ==RESULT_OK)
        {
            if (it.data !=null)
            {
            val imageUri = it.data?.data
            try
                {
                val inputStream : InputStream? =
                    imageUri?.let { it1 -> contentResolver.openInputStream(it1) };
                val bitmap : Bitmap = BitmapFactory.decodeStream(inputStream);
                binding.imageProfile.setImageBitmap(bitmap);
                binding.tvAddImage.visibility = View.GONE
                encodedImageString = encodedImage(bitmap)
                }
            catch(e : FileNotFoundException) { e.printStackTrace()}
            }
        }
    }


    //    Check for valid sign-up input
    private fun isValidSignUp() : Boolean
    {
        Log.d("Invalid sign up", "Is empty?")
        if (encodedImageString.equals("empty"))
        {
            raiseToast("Please select a profile picture.")
            return false
        }
        else if (binding.inputName.text.trim().isNullOrEmpty())
        {
            raiseToast("Enter a valid name.")
            return false
        }
        else if (binding.inputEmail.text.trim().isNullOrEmpty())
        {
            raiseToast("Enter a an email address.")
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.trim()).matches())
        {
            raiseToast("Please enter a valid email.")
            return false
        }
        else if (binding.inputPassword.text.isNullOrEmpty())
        {
            raiseToast("Password must not be empty.")
            return false
        }

        else if(binding.inputPassword.text.trim() != binding.inputConfirmPassword.text.trim())
        {
            Log.e("Password","1st: ${binding.inputPassword.text.trim()}, second: ${binding.inputConfirmPassword.text.trim()}")
            raiseToast("Password mismatch.")
            return false
        }
        Log.d("Invalid sign up", "returning true")
        return true
    }

    private fun isLoading(loading : Boolean) = if (loading)
    {
        binding.buttonSignUp.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }
    else
    {
        binding.progressBar.visibility = View.INVISIBLE
        binding.buttonSignUp.visibility = View.VISIBLE

    }

}


