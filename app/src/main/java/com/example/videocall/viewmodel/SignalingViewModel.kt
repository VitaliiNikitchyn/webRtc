package com.example.videocall.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.videocall.model.DBField
import com.example.videocall.R
import com.example.videocall.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignalingViewModel(context: Application) : AndroidViewModel(context) {
    private val dbUsers = Firebase.firestore.collection(DBField.COLLECTION_USERS)
    private val pref = context.getSharedPreferences(
            context.resources.getString(R.string.main_preferences), Context.MODE_PRIVATE)

    private val myUserID = MutableLiveData<String>(pref.getString(DBField.MY_USER_ID, null))
    private val incomingCallOffer = CallSignalingLiveData(DBField.CALL_OFFER)
    private val outgoingCallOffer = CallSignalingLiveData(DBField.CALL_ANSWER)

    init {
        myUserID.value?.let { incomingCallOffer.subscribe(it) }
    }

    fun getIncomingCallRequest(): LiveData<String?> = incomingCallOffer
    fun getOutgoingCallRequest(): LiveData<String?> = outgoingCallOffer

    fun sendCallRequest(addresseeID: String, sdp: String) {
        outgoingCallOffer.subscribe(addresseeID)
        dbUsers.document(addresseeID).update(mapOf(DBField.CALL_OFFER to sdp))
    }

    fun cancelCallRequest() {
        outgoingCallOffer.unsubscribe()
    }

    fun answerCall(sdp: String) {
        myUserID.value?.let { dbUsers.document(it).update(mapOf(DBField.CALL_ANSWER to sdp)) }
    }

    fun rejectCall() {
        myUserID.value?.let { dbUsers.document(it).update(mapOf(DBField.CALL_OFFER to FieldValue.delete())) }
    }

    fun clearSignals() {
        myUserID.value?.let {
            dbUsers.document(it).update(mapOf(DBField.CALL_ANSWER to FieldValue.delete()))
            dbUsers.document(it).update(mapOf(DBField.CALL_OFFER to FieldValue.delete()))
        }
    }

    fun setMyOnlineStatus(status: Boolean) {
        myUserID.value?.let { dbUsers.document(it).update(DBField.IS_ACTIVE, status) }
    }

    fun getMyUserID(): LiveData<String> = myUserID

    fun login(name: String) {
        dbUsers.add(mapOf("name" to name)).addOnSuccessListener {
            myUserID.value = it.id
            pref.edit().putString(DBField.MY_USER_ID, it.id).apply()
            incomingCallOffer.subscribe(it.id)
        }
    }

    fun logOut() {
        myUserID.value?.let {
            incomingCallOffer.unsubscribe()
            dbUsers.document(it).delete()
        }
        pref.edit().remove(DBField.MY_USER_ID).apply()
        myUserID.value = null
    }

    //should not be here
    fun getActiveUsers() {
        dbUsers.whereEqualTo(DBField.IS_ACTIVE, true).addSnapshotListener { snapshots, error ->
            if (snapshots == null) return@addSnapshotListener
            val users = snapshots.map { doc -> User(name = doc.getString("name")!!, doc.id) }

/*            for (dc in snapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        if (dc.document.id != myUserID.value!!)
                            users.add(User(dc.document.getString("name")!!, dc.document.id))
                            //adapter.addItem(User(dc.document.getString("name")!!, dc.document.id))
                    }
                    DocumentChange.Type.MODIFIED -> {} //Log.d(TAG, "Modified: ${dc.document.data}")
                    DocumentChange.Type.REMOVED -> {
                        User(dc.document.getString("name")!!, dc.document.id)
                        //adapter.removeItem(dc.document.id)
                    }
                }
            }*/
        }
    }
}