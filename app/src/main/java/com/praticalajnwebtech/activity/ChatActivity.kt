package com.praticalajnwebtech.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.praticalajnwebtech.R
import com.praticalajnwebtech.adapter.ChatAdapter
import com.praticalajnwebtech.model.ChatModel
import com.praticalajnwebtech.model.UserModel
import com.praticalajnwebtech.utils.CommanUtils.hideSoftKeyboard
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_user_data.toolbar
import kotlinx.android.synthetic.main.item_user_data.view.*

class ChatActivity : AppCompatActivity(), ChatAdapter.OnItemClickListener {
    private var userId: String? = null
    private var mFirebaseUser: FirebaseUser? = null
    private lateinit var chatAdapter: ChatAdapter
    private var mChatModel: ArrayList<ChatModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        getIntentData()
        setSupportActionBar(toolbar)
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        getCurrentUSer()
        defineListeners()
    }


    private fun defineListeners() {
        imgsend.setOnClickListener {
            if (!edtTypeMessage.text.toString().equals("")) {
                sendMessage(mFirebaseUser!!.uid, userId!!, edtTypeMessage.text.toString())
            } else {
                Toast.makeText(
                    this@ChatActivity,
                    "Your can not send empty message",
                    Toast.LENGTH_LONG
                ).show()
            }
            hideSoftKeyboard(this@ChatActivity)
            edtTypeMessage.setText("")
        }

    }

    private fun getIntentData() {
        if (intent.extras != null) {
            userId = intent.getStringExtra("userId")
        }


    }

    private fun getCurrentUSer() {
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId!!)
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel = dataSnapshot.getValue(UserModel::class.java)
                if (userModel?.profilePic!!.startsWith("https")) {
                    Glide.with(this@ChatActivity)
                        .load(userModel.profilePic!!)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .into(imgUserAvatar)
                } else {
                    val decodedString: ByteArray = Base64.decode(userModel.profilePic!!, Base64.DEFAULT)
                    val image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    Glide.with(this@ChatActivity)
                        .load(image)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .into(imgUserAvatar)
                }
                txtTitle.text = userModel?.first_name + " " + userModel?.last_name
                readChat(mFirebaseUser!!.uid, userId!!, userModel!!.profilePic)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ChatActivity, databaseError.message, Toast.LENGTH_LONG).show()

            }

        })
    }


    private fun sendMessage(sender: String, receiver: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val chatModel = ChatModel()
        chatModel.message = message
        chatModel.receiver = receiver
        chatModel.sender = sender
        // val chatModel = ChatModel(sender, receiver, message)
        reference.child("Chats").push().setValue(chatModel)
    }


    private fun readChat(myId: String, userId: String, imgUrl: String) {
        progressBarChat.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance().getReference("Chats")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mChatModel.clear()
                for (i in dataSnapshot.children) {
                    val chatModel = i.getValue(ChatModel::class.java)
                    if (chatModel?.receiver.equals(myId) && chatModel?.sender.equals(userId) || chatModel?.receiver.equals(userId) && chatModel?.sender.equals(myId))
                        mChatModel.add(chatModel!!)

                }
                progressBarChat.visibility = View.GONE
                chatAdapter = ChatAdapter(this@ChatActivity, mChatModel, imgUrl, this@ChatActivity)
                rvChatList.layoutManager = LinearLayoutManager(this@ChatActivity)
                rvChatList.adapter = chatAdapter


            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBarChat.visibility = View.GONE
                Toast.makeText(this@ChatActivity, databaseError.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    companion object {
        fun callChatActivity(context: Context, userId: String) {
            val mIntent = Intent(context, ChatActivity::class.java)
            mIntent.putExtra("userId", userId)
            context.startActivity(mIntent)
        }
    }

    override fun onItemClick(position: Int) {

    }

}
