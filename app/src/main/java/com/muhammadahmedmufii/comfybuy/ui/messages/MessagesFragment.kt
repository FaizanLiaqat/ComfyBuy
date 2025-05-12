// --- ui/messages/MessagesFragment.kt --- (New package or your existing UI package)
package com.muhammadahmedmufii.comfybuy.ui.messages // Or your preferred package

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muhammadahmedmufii.comfybuy.ChatActivity
import com.muhammadahmedmufii.comfybuy.MessageItem
import com.muhammadahmedmufii.comfybuy.R
import com.muhammadahmedmufii.comfybuy.databinding.FragmentMessagesBinding // Use ViewBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var viewModel: MessagesViewModel // To be created

    // Sample data - will be replaced by ViewModel LiveData
    private var currentMessageList = mutableListOf<MessageItem>()

    companion object {
        fun newInstance() = MessagesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        // val factory = MessagesViewModelFactory(requireActivity().application) // Create this factory
        // viewModel = ViewModelProvider(this, factory).get(MessagesViewModel::class.java)
        // For now, without ViewModel, using sample data:
        Log.d("MessagesFragment", "onViewCreated")

        setupRecyclerView()
        loadSampleMessages() // Load sample data initially
        setupSearch()

        // observeViewModel() // Call this when ViewModel is ready
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter { selectedMessageItem -> // Changed from position to item
            Log.d("MessagesFragment", "Chat item clicked: ${selectedMessageItem.name}")
            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("CHAT_NAME", selectedMessageItem.name)
                // Pass actual profile pic data (URL or Base64) instead of drawable ID for real data
                // For sample data, drawable ID is fine.
                putExtra("CHAT_PROFILE_PIC_RES_ID", selectedMessageItem.profilePicResId) // Assuming profilePicResId in MessageItem
                putExtra("CHAT_USER_ID", selectedMessageItem.userId) // Pass userId for the chat
            }
            startActivity(intent)
        }
        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }
    }

    private fun loadSampleMessages() {
        // This is temporary. Real data will come from ViewModel.
        currentMessageList.clear()
        currentMessageList.addAll(
            listOf(
                MessageItem(
                    "user123", // Added userId
                    "Emily Parker",
                    "The vintage lamp is still available?",
                    "2m ago",
                    R.drawable.avatar_placeholder, // Changed to profilePicResId
                    true
                ),
                MessageItem(
                    "user456",
                    "Michael Chen",
                    "Great! I'll pick it up tomorrow",
                    "1h ago",
                    R.drawable.avatar_placeholder,
                    false
                ),
                MessageItem(
                    "user789",
                    "Sophie Williams",
                    "Can you do $40 for the chair?",
                    "2h ago",
                    R.drawable.avatar_placeholder,
                    true
                )
            )
        )
        messageAdapter.submitList(currentMessageList.toList()) // Submit a copy
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMessages(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterMessages(query: String) {
        if (query.isEmpty()) {
            messageAdapter.submitList(currentMessageList.toList())
        } else {
            val filteredList = currentMessageList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.message.contains(query, ignoreCase = true)
            }
            messageAdapter.submitList(filteredList)
        }
    }

    // private fun observeViewModel() {
    //     viewModel.chatList.observe(viewLifecycleOwner) { messages ->
    //         Log.d("MessagesFragment", "Observed chat list, size: ${messages.size}")
    //         currentMessageList.clear()
    //         currentMessageList.addAll(messages)
    //         messageAdapter.submitList(messages) // Use ListAdapter's submitList
    //     }
    // }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}