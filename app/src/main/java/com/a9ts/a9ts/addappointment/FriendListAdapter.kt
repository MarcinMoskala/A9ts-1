package com.a9ts.a9ts.addappointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AddFriendsItemBinding
import com.a9ts.a9ts.model.Friend

class FriendListAdapter(
    private val friends: List<Friend>,
    val onClick: (friendUserId: String, friendFullName: String) -> Unit,
    val onClickInvited: (friendUserId: String) -> Unit) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // DONE askmarcin not sure how this let works... it creates a ViewHolder with this binding as parameter... not sure why
        return AddFriendsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
//            .let { ViewHolder(it) }
//            .let { it -> ViewHolder(it) }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
        when (friends[position].state) {
            Friend.STATE_I_INVITED -> holder.itemView.setOnClickListener { onClickInvited(friends[position].fullName!!)}
            Friend.STATE_ACCEPTED -> holder.itemView.setOnClickListener { onClick(friends[position].authUserId!!, friends[position].fullName!!) }
        }
    }


    override fun getItemCount() = friends.size

    class ViewHolder(private val itemBinding: AddFriendsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(friend: Friend) {
            itemBinding.apply {
                fullname.text = friend.fullName

                if (friend.state == Friend.STATE_I_INVITED) {
                    button.text = "Invited"
                    button.isEnabled = false
                    button.visibility = View.VISIBLE
                } else {
                    button.visibility = View.GONE
                }
            }
        }
    }

}



