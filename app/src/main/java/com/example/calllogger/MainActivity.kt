package com.example.calllogger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        if (checkPermissions()) {
            tvStatus.text = "Permissions granted. The app is running in the background and logging calls."
        } else {
            tvStatus.text = "Requesting permissions..."
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val phoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        val callLog = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
        return phoneState == PackageManager.PERMISSION_GRANTED && callLog == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                tvStatus.text = "Permissions granted. The app is running in the background and logging calls."
                Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show()
            } else {
                tvStatus.text = "Permissions denied. The app cannot log calls."
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
