//package com.muhammadahmedmufii.comfybuy
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import de.hdodenhof.circleimageview.CircleImageView
//
//class FollowingAdapter(private val users: List<FollowUser>) :
//    RecyclerView.Adapter<FollowingAdapter.FollowViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_following, parent, false)
//        return FollowViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: FollowViewHolder, position: Int) {
//        val user = users[position]
//        holder.bind(user)
//    }
//
//    override fun getItemCount(): Int = users.size
//
//    inner class FollowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val profilePic: CircleImageView = itemView.findViewById(R.id.ivProfilePic)
//        private val name: TextView = itemView.findViewById(R.id.tvName)
//        private val username: TextView = itemView.findViewById(R.id.tvUsername)
//        private val followButton: Button = itemView.findViewById(R.id.btnFollow)
//
//        fun bind(user: FollowUser) {
//            name.text = user.name
//            username.text = user.username
//            profilePic.setImageResource(user.profilePic)
//
//            if (user.isFollowing) {
//                followButton.text = "Following"
//                followButton.setBackgroundResource(R.drawable.bg_button_following)
//            } else {
//                followButton.text = "Follow"
//                followButton.setBackgroundResource(R.drawable.bg_button_follow)
//            }
//
//            followButton.setOnClickListener {
//                val context = itemView.context
//                if (user.isFollowing) {
//                    // Unfollow user
//                    user.isFollowing = false
//                    followButton.text = "Follow"
//                    followButton.setBackgroundResource(R.drawable.bg_button_follow)
//                    Toast.makeText(context, "Unfollowed ${user.name}", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Follow user
//                    user.isFollowing = true
//                    followButton.text = "Following"
//                    followButton.setBackgroundResource(R.drawable.bg_button_following)
//                    Toast.makeText(context, "Following ${user.name}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}
