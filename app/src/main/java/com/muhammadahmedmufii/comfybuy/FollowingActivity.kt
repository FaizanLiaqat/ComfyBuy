package com.muhammadahmedmufii.comfybuy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class FollowingActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var followingAdapter: FollowingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupTabLayout()
        setupRecyclerView()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerViewFollowing)
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> loadFollowers()
                    1 -> loadFollowing()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Select "Following" tab by default
        tabLayout.getTabAt(1)?.select()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadFollowing() // Load following list by default
    }

    private fun loadFollowers() {
        val followersList = getFollowersList()
        followingAdapter = FollowingAdapter(followersList)
        recyclerView.adapter = followingAdapter
    }

    private fun loadFollowing() {
        val followingList = getFollowingList()
        followingAdapter = FollowingAdapter(followingList)
        recyclerView.adapter = followingAdapter
    }

    private fun getFollowersList(): List<FollowUser> {
        return listOf(
            FollowUser("Emma Wilson", "@emmaw", R.drawable.avatar_placeholder, true),
            FollowUser("Alex Thompson", "@alexthompson", R.drawable.avatar_placeholder, true),
            FollowUser("Jessica Lee", "@jesslee", R.drawable.avatar_placeholder, false)
        )
    }

    private fun getFollowingList(): List<FollowUser> {
        return listOf(
            FollowUser("Emma Wilson", "@emmaw", R.drawable.avatar_placeholder, true),
            FollowUser("Alex Thompson", "@alexthompson", R.drawable.avatar_placeholder, true),
            FollowUser("Jessica Lee", "@jesslee", R.drawable.avatar_placeholder, false)
        )
    }
}
