package com.example.videocall.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.videocall.R
import com.example.videocall.viewmodel.SignalingViewModel

class MainActivity : AppCompatActivity() {
    private val requestCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
    }

    private fun initApp() {
        val myID = ViewModelProvider(this).get(SignalingViewModel::class.java).getMyUserID().value
        if (myID == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container_view, LoginFragment.newInstance(), LoginFragment.TAG)
                    .commit()
        } else {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container_view, MainFragment.newInstance(), MainFragment.TAG)
                    .commit()
        }
    }

    private fun requestPermissions() {
        val p1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val p2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (p1 == PackageManager.PERMISSION_GRANTED && p2 == PackageManager.PERMISSION_GRANTED) {
            initApp()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), requestCode)
        }
    }

    override fun onRequestPermissionsResult(rCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(rCode, permissions, grantResults)
        if (rCode == requestCode && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initApp()
        } else {
            Toast.makeText(this, "Permissions were not granted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}