package com.example.videocall.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.videocall.model.ConnectionMode

// used by CallFragment to correctly react on actions from signaling server
class ConnectionViewModel : ViewModel() {
    private val currentConnectionMode = MutableLiveData<ConnectionMode>(ConnectionMode.Unconnected)

    fun setConnectionMode(mode: ConnectionMode) {
        currentConnectionMode.value = mode
    }

    fun getCurrentConnectionMode(): LiveData<ConnectionMode> = currentConnectionMode
}