package com.a9ts.a9ts.addappointment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.FriendsItemBinding
import com.a9ts.a9ts.model.User
import timber.log.Timber

class FriendListAdapter (private val friends:List<User>, val onClick: (friendUserId: String, friendFullName: String) -> Unit) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return FriendsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
        holder.itemView.setOnClickListener { onClick(friends[position].authUserId!!, friends[position].fullName!!) }

    }


    override fun getItemCount() = friends.size

    class ViewHolder(private val itemBinding : FriendsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(user: User) {
            itemBinding.apply {
                fullname.text = user.fullName
                telephone.text = user.telephone
            }
        }
    }

}



