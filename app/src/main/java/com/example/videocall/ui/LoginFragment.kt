package com.example.videocall.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.example.videocall.R
import com.example.videocall.viewmodel.SignalingViewModel

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val userNameEdit = view.findViewById<EditText>(R.id.username)
        val progressBar = view.findViewById<ProgressBar>(R.id.login_progress)

        val userVM = ViewModelProvider(requireActivity()).get(SignalingViewModel::class.java)
        view.findViewById<Button>(R.id.loginBtn).setOnClickListener { btn ->
            progressBar.visibility = View.VISIBLE
            btn.isClickable = false
            userVM.login(userNameEdit.text.toString())
        }

        userVM.getMyUserID().observe(viewLifecycleOwner) { myUserID ->
            if (myUserID == null) return@observe
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, MainFragment.newInstance(), MainFragment.TAG)
                    .commit()
        }

        return view
    }

    companion object {
        const val TAG = "LoginFragment"
        fun newInstance() = LoginFragment()
    }
}