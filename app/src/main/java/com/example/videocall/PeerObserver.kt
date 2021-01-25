package com.example.videocall

import org.webrtc.*

open class PeerObserver : PeerConnection.Observer {
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

    override fun onIceCandidate(p0: IceCandidate?) {}

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        super.onSelectedCandidatePairChanged(event)
    }

    override fun onAddStream(p0: MediaStream?) {}

    override fun onRemoveStream(p0: MediaStream?) {}

    override fun onDataChannel(p0: DataChannel?) {}

    override fun onRenegotiationNeeded() {}

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
    }
}