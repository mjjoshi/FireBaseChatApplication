package com.praticalajnwebtech.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.praticalajnwebtech.R
import com.praticalajnwebtech.activity.ChatActivity.Companion.callChatActivity
import com.praticalajnwebtech.adapter.UserDataAdapter
import com.praticalajnwebtech.model.UserModel
import kotlinx.android.synthetic.main.activity_user_data.*
import kotlinx.android.synthetic.main.activity_user_data.progressBar


class UserDataActivity : AppCompatActivity(), UserDataAdapter.OnItemClickListener {

    private var mFirebaseUser: FirebaseUser? = null
    private lateinit var userDataAdapter: UserDataAdapter
    private var muserModel: ArrayList<UserModel> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        getCurrentUSer()
        readData()


    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()

    }

    private fun getCurrentUSer() {
        val database = FirebaseDatabase.getInstance().getReference("users").child(mFirebaseUser!!.uid)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel = dataSnapshot.getValue(UserModel::class.java)
                setSupportActionBar(toolbar)
                supportActionBar?.title = userModel?.first_name + " " + userModel?.last_name
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }

        })
    }

    private fun readData() {
        progressBar.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                muserModel.clear()
                for (i in dataSnapshot.children) {
                    val userModel = i.getValue(UserModel::class.java)
                    if (!userModel?.user_id.equals(mFirebaseUser!!.uid)) {
                        muserModel.add(userModel!!)
                    }
                }
                progressBar.visibility = View.GONE
                userDataAdapter = UserDataAdapter(this@UserDataActivity, muserModel, this@UserDataActivity)
                rvUserList.layoutManager = LinearLayoutManager(this@UserDataActivity)
                rvUserList.adapter = userDataAdapter


            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@UserDataActivity, databaseError.message, Toast.LENGTH_LONG).show()
            }

        })


    }


    companion object {
        fun callUserDataActivity(context: Context) {
            val mIntent = Intent(context, UserDataActivity::class.java)
            context.startActivity(mIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logOut -> {
                FirebaseAuth.getInstance().signOut()
                LoginActivity.callLoginActivity(this@UserDataActivity)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(position: Int) {
        callChatActivity(this@UserDataActivity, muserModel[position].user_id)
    }
}