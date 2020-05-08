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
import com.praticalajnwebtech.R
import com.praticalajnwebtech.model.UserModel
import kotlinx.android.synthetic.main.item_user_data.view.*


class UserDataAdapter(
    private val context: Context,
    private val itemList: List<UserModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var VIEWTYPE_ITEM = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_data, parent, false)
        return MyViewHolder(itemView)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val item = itemList[position]
            holder.itemView.txtName.text = item.first_name + " " + item.last_name
            if (item.profilePic.startsWith("https")) {
                Glide.with(context)
                    .load(item.profilePic)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(holder.itemView.profile_image)
            } else {
                val decodedString: ByteArray = Base64.decode(item.profilePic, Base64.DEFAULT)
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
        return VIEWTYPE_ITEM

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
