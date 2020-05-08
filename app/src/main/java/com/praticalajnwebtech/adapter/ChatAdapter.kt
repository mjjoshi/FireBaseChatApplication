package com.praticalajnwebtech.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.praticalajnwebtech.R
import com.praticalajnwebtech.model.ChatModel
import kotlinx.android.synthetic.main.chat_item_left.view.*
import kotlinx.android.synthetic.main.item_user_data.view.*
import kotlinx.android.synthetic.main.item_user_data.view.profile_image


class ChatAdapter(
    private val context: Context,
    private val itemList: List<ChatModel>,
    private val imgUrl: String,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var MSG_LEFT = 1
    private var MSG_RIGHT = 2
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MSG_RIGHT) {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_right, parent, false)
            return MyViewHolder(itemView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_left, parent, false)
            return MyViewHolder(itemView)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val item = itemList[position]
            holder.itemView.txtMessage.text = item.message
//            Glide.with(context)
//                .load(imgUrl)
//                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
//                .into(holder.itemView.profile_image)


            if (imgUrl.startsWith("https")) {
                Glide.with(context)
                    .load(imgUrl)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(holder.itemView.profile_image)
            } else {
                val decodedString: ByteArray = Base64.decode(imgUrl, Base64.DEFAULT)
                val image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                Glide.with(context)
                    .load(image)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(holder.itemView.profile_image)
            }


            holder.itemView.setOnClickListener {
                listener.onItemClick(position)
            }
        }

    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (itemList.get(position).sender.equals(mFirebaseUser!!.uid)) {
            return MSG_RIGHT
        } else {
            return MSG_LEFT
        }

    }

}
