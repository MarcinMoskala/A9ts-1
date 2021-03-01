package com.a9ts.a9ts.addfriends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AddFriendsItemBinding
import com.a9ts.a9ts.model.Friend

class AddFriendsListAdapter(private var friendList: List<Friend> = listOf(), private val onClick: (userId : String, position: Int) -> Unit
) : RecyclerView.Adapter<AddFriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AddFriendsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendList[position], position, onClick)
    }

    fun updateList(newList : List<Friend>) {
        friendList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = friendList.size

    fun setButtonToSent(viewHolderPosition: Int) {
        friendList[viewHolderPosition].state = Friend.STATE_I_INVITED
        notifyItemChanged(viewHolderPosition)
    }

    class ViewHolder(private val itemBinding : AddFriendsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(friend : Friend, position: Int, onClick: (userId : String, position: Int) -> Unit) {
            itemBinding.apply {
                fullname.text = friend.fullName

                if (friend.state == Friend.STATE_I_INVITED) {
                    button.text = "Invited"
                    button.isEnabled = false
                }
                button.setOnClickListener { onClick(friend.authUserId!!, position) } //send userId
            }
        }
    }

}



