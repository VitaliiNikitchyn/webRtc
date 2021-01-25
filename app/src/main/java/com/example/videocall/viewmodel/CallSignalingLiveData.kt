package com.example.videocall.viewmodel

import androidx.lifecycle.LiveData
import com.example.videocall.model.DBField
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CallSignalingLiveData(private val dbField: String) : LiveData<String?>() {
    private val dbUsers = Firebase.firestore.collection(DBField.COLLECTION_USERS)
    private var registration: ListenerRegistration? = null

    fun subscribe(docID: String) {
        if (registration != null) {
            unsubscribe()
        }
        //register
        registration = dbUsers.document(docID).addSnapshotListener { snapshot, error ->
            if (snapshot != null && !snapshot.metadata.hasPendingWrites()) {  //from Server
                //val incoming = snapshot.getString(DBField.CALL_OFFER)
                postValue(snapshot.getString(dbField))
            }
        }
    }

    fun unsubscribe() {
        registration?.remove()
        registration = null
        postValue(null)
    }

    override fun onInactive() {
        unsubscribe()
        super.onInactive()
    }
}