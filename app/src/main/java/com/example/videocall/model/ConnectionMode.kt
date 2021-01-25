package com.example.videocall.model

sealed class ConnectionMode {
    class Outgoing(val addresseeUserID: String): ConnectionMode()
    class Incoming(val sdp: String): ConnectionMode()
    object Unconnected: ConnectionMode()
}