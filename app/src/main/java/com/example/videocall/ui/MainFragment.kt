package com.example.videocall.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videocall.model.User
import com.example.videocall.model.DBField
import com.example.videocall.R
import com.example.videocall.adapter.ActiveUsersAdapter
import com.example.videocall.model.ConnectionMode
import com.example.videocall.viewmodel.ConnectionViewModel
import com.example.videocall.viewmodel.SignalingViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainFragment : Fragment() {
    private val dbUsers = Firebase.firestore.collection(DBField.COLLECTION_USERS)
    private var callRequestDialog: AlertDialog? = null
    private lateinit var activeUserList: RecyclerView

    private lateinit var signalingViewModel: SignalingViewModel
    private lateinit var reg1: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.findViewById<Toolbar>(R.id.toolbar_main).setOnMenuItemClickListener {
            onMenuItemSelected(it)
        }

        val layoutManager = LinearLayoutManager(activity)
        activeUserList = view.findViewById(R.id.active_users)
        activeUserList.layoutManager = layoutManager
        activeUserList.addItemDecoration(DividerItemDecoration(activeUserList.context, layoutManager.orientation))
        activeUserList.adapter = ActiveUsersAdapter {
            startOutGoingCall(it.id)
        }
        return view
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_group_call -> {
                Toast.makeText(requireContext(), "Not yet implemented", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_logout -> {
                signalingViewModel.logOut()
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signalingViewModel = ViewModelProvider(requireActivity()).get(SignalingViewModel::class.java)
        listenActiveUsers()
        listenIncomingCall()
    }

    override fun onResume() {
        super.onResume()
        signalingViewModel.setMyOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        signalingViewModel.setMyOnlineStatus(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reg1.remove()
        (activeUserList.adapter as ActiveUsersAdapter).clear()
    }

    private fun listenActiveUsers() {
        // should not be here: move to viewModel
        reg1 = dbUsers.whereEqualTo(DBField.IS_ACTIVE, true).addSnapshotListener { snapshots, error ->
            if (snapshots == null) return@addSnapshotListener
            val adapter = activeUserList.adapter as ActiveUsersAdapter
            for (dc in snapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        if (dc.document.id != signalingViewModel.getMyUserID().value!!)
                            adapter.addItem(User(dc.document.getString("name")!!, dc.document.id))
                    }
                    DocumentChange.Type.MODIFIED -> {} //Log.d(TAG, "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> {
                        adapter.removeItem(dc.document.id)
                    }
                }
            }
        }
    }

    private fun listenIncomingCall() {
        signalingViewModel.getIncomingCallRequest().observe(viewLifecycleOwner) { incomingSdp ->
            if (incomingSdp != null) {
                val ctx = requireActivity()
                callRequestDialog = AlertDialog.Builder(ctx)
                        .setCancelable(false)
                        .setTitle("Someone is calling")
                        .setPositiveButtonIcon(ActivityCompat.getDrawable(ctx, R.drawable.ic_call_start_24))
                        .setNegativeButtonIcon(ActivityCompat.getDrawable(ctx, R.drawable.ic_call_end_24))
                        .setPositiveButton("") { dialog, id ->
                            // start incoming call
                            ViewModelProvider(requireActivity()).get(ConnectionViewModel::class.java)
                                    .setConnectionMode(ConnectionMode.Incoming(incomingSdp))
                            startCallFragment()
                        }
                        .setNegativeButton("") { dialog, id ->
                            signalingViewModel.rejectCall()
                        }
                        .create()
                callRequestDialog?.show()
            } else {    // caller aborted the call
                callRequestDialog?.cancel()
                //callRequestDialog = null
            }
        }
    }

    private fun startOutGoingCall(addresseeID: String) {
        ViewModelProvider(requireActivity())
                .get(ConnectionViewModel::class.java)
                .setConnectionMode(ConnectionMode.Outgoing(addresseeID))
        startCallFragment()
    }

    private fun startCallFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container_view, CallFragment.newInstance(), CallFragment.TAG)
                .commit()
    }

    companion object {
        const val TAG = "MainFragment"
        fun newInstance() = MainFragment()
    }
}