package com.a9ts.a9ts.addfriends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AddFriendsItemBinding
import com.a9ts.a9ts.databinding.FriendsItemBinding
import com.a9ts.a9ts.model.User

class AddFriendsListAdapter (private val userList:List<User>
//, val onClick: (friendUserId: String, friendFullName: String) -> Unit
) : RecyclerView.Adapter<AddFriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AddFriendsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
//        holder.itemView.setOnClickListener { onClick(userList[position].authUserId!!, userList[position].fullName!!) }

    }


    override fun getItemCount() = userList.size

    class ViewHolder(private val itemBinding : AddFriendsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(user: User) {
            itemBinding.apply {
                fullname.text = user.fullName
            }
        }
    }

}



