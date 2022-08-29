package com.example.safespace.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.adapters.UsersAdapter
import com.example.safespace.databinding.ActivityUserListBinding
import com.example.safespace.listeners.UserListener
import com.example.safespace.models.User
import com.example.safespace.utilities.Constants
import com.example.safespace.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity(), UserListener  {
    private lateinit var binding : ActivityUserListBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
        getUsers()

    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener { onBackPressed()}
    }

    private fun getUsers(){
        var userList : MutableList<User> = mutableListOf<User>()
        isLoading(true)
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                isLoading(false)
                val currentUserID = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null)
                {
                    for (snapshot in it.result) {
                        if (currentUserID.equals(snapshot.id)) {
                            continue
                        }
                        val user = User()
                        user.name = snapshot.getString(Constants.KEY_NAME).toString()
                        user.email = snapshot.getString(Constants.KEY_EMAIL).toString()
                        user.image = snapshot.getString(Constants.KEY_IMAGE).toString()
                        user.token = snapshot.getString(Constants.KEY_FIRESTORECLOUDMESSAGING_TOKEN)
                            .toString()
                        user.id = snapshot.id
                        userList += user
                    }
                    if (userList != null && userList.size > 0) {
                        val userAdapter = UsersAdapter(userList,this)
                        binding.recycleviewUsers.adapter = userAdapter
                        binding.recycleviewUsers.visibility = View.VISIBLE
                    } else
                    {
                        showErrorMessage()
                    }
                }
                else
                {
                    showErrorMessage()
                }
            }
    }

    private fun isLoading(loading : Boolean) = if (loading)
    {
        binding.progressBar.visibility = View.VISIBLE
    }
    else
    {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showErrorMessage()
    {
        binding.tvErrorMsg.text = String.format("%s","no user available")
        binding.tvErrorMsg.visibility = View.VISIBLE

    }

    override fun onUserClicked(user: User) {
        val intent = Intent(this,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()
    }
}