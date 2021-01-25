package com.example.videocall.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.videocall.R
import com.example.videocall.RtcClient
import com.example.videocall.model.ConnectionMode
import com.example.videocall.viewmodel.ConnectionViewModel
import com.example.videocall.viewmodel.SignalingViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*

class CallFragment : Fragment() {
    private lateinit var rtcClient: RtcClient
    private lateinit var localView: SurfaceViewRenderer
    private lateinit var remoteView: SurfaceViewRenderer
    private lateinit var signalingVM: SignalingViewModel
    private lateinit var connectionVM: ConnectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_call, container, false)
        remoteView = view.findViewById(R.id.remote_view)
        localView = view.findViewById(R.id.local_view)
        view.findViewById<FloatingActionButton>(R.id.end_call).setOnClickListener {
            cancelFragment()
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signalingVM = ViewModelProvider(requireActivity()).get(SignalingViewModel::class.java)
        initPeer()

        connectionVM = ViewModelProvider(requireActivity()).get(ConnectionViewModel::class.java)
        connectionVM.getCurrentConnectionMode().observe(viewLifecycleOwner) {
            when (val connectionMode = it) {
                is ConnectionMode.Outgoing -> {
                    rtcClient.call(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            signalingVM.sendCallRequest(connectionMode.addresseeUserID, sdp!!.description)
                        }
                        override fun onSetSuccess() { }
                        override fun onCreateFailure(p0: String?) { }
                        override fun onSetFailure(p0: String?) { }
                    })
                    listenCallAccept()
                }
                is ConnectionMode.Incoming -> {
                    val session = SessionDescription(SessionDescription.Type.OFFER, connectionMode.sdp)
                    rtcClient.onRemoteSessionReceived(session)
                    rtcClient.answer(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            signalingVM.answerCall(sdp!!.description)
                        }
                        override fun onSetSuccess() { }
                        override fun onCreateFailure(p0: String?) { }
                        override fun onSetFailure(p0: String?) { }
                    })
                    listenIceChange()
                }
                ConnectionMode.Unconnected -> {}
            }
        }
    }

    private fun cancelFragment() {
        val fm = requireActivity().supportFragmentManager
        val fragment = fm.findFragmentByTag(TAG)
        if (fragment != null && fragment.isVisible)
            fm.beginTransaction().remove(fragment).commit()
    }

    private fun listenCallAccept() {
        signalingVM.getOutgoingCallRequest().observe(viewLifecycleOwner) {
            if (it == null) {   // call aborted
                //cancelFragment()
            } else {    // call accepted
                val session = SessionDescription(SessionDescription.Type.ANSWER, it)
                rtcClient.onRemoteSessionReceived(session)
            }
        }
    }

    private fun listenIceChange() {
        val mm = connectionVM.getCurrentConnectionMode().value
        val collection = if (mm is ConnectionMode.Incoming) "IceCandidates1" else "IceCandidates"

        Firebase.firestore.collection(collection).addSnapshotListener { snapshots, error ->
            if (snapshots == null) return@addSnapshotListener
            for (dc in snapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        rtcClient.addIceCandidate(IceCandidate(
                                dc.document.getString("sdpMid"),
                                dc.document.getLong("sdpMLineIndex")!!.toInt(),
                                dc.document.getString("sdp")
                        ))
                    }
                    DocumentChange.Type.MODIFIED -> { } //Log.d(TAG, "Modified city: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> { }
                }
            }
        }
    }

    private fun initPeer() {
        val peerObserver = object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}

            override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                super.onStandardizedIceConnectionChange(newState)
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {}

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}

            override fun onIceCandidate(ice: IceCandidate?) {
                if (ice == null) return

                val mm = connectionVM.getCurrentConnectionMode().value
                val collection = if (mm is ConnectionMode.Incoming) "IceCandidates" else "IceCandidates1"
                Firebase.firestore.collection(collection)
                        .document()
                        .set(mapOf(
                                "sdpMid" to ice.sdpMid,
                                "sdpMLineIndex" to ice.sdpMLineIndex,
                                "sdp" to ice.sdp
                        ))

                rtcClient.addIceCandidate(ice)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

            override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
                super.onSelectedCandidatePairChanged(event)
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                val remoteVideoTrack = mediaStream?.videoTracks?.get(0)
                //val remoteAudioTrack = mediaStream?.audioTracks?.get(0)
                //remoteAudioTrack?.setEnabled(true)
                remoteVideoTrack?.setEnabled(true)
                //remoteVideoTrack?.addRenderer(VideoRenderer(binding.surfaceView2))

                remoteVideoTrack?.addSink(remoteView)
            }

            override fun onRemoveStream(p0: MediaStream?) {}

            override fun onDataChannel(p0: DataChannel?) {}

            override fun onRenegotiationNeeded() {}

            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}

            override fun onTrack(transceiver: RtpTransceiver?) {
                super.onTrack(transceiver)
            }
        }
        rtcClient = RtcClient(requireContext(), peerObserver)
        rtcClient.initSurfaceView(remoteView)
        rtcClient.initSurfaceView(localView)
        rtcClient.startLocalVideoCapture(localView)
    }

    override fun onDestroyView() {
        signalingVM.clearSignals()
        connectionVM.setConnectionMode(ConnectionMode.Unconnected)
        rtcClient.close()
        super.onDestroyView()
    }

    companion object {
        const val TAG = "CallFragment"
        fun newInstance() = CallFragment()
    }
}