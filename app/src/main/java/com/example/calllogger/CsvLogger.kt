package com.example.calllogger

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvLogger {
    private const val TAG = "CsvLogger"

    fun logCall(context: Context, number: String?, type: String, duration: String?, date: Long) {
        try {
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (fileDir != null && !fileDir.exists()) {
                fileDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormat.format(Date(date))
            val fileName = "$dateString-calls.csv"
            
            val file = File(fileDir, fileName)
            val isNewFile = !file.exists()
            
            val writer = FileWriter(file, true)
            
            if (isNewFile) {
                writer.append("Date Time,Number,Type,Duration (s)\n")
            }
            
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timeString = timeFormat.format(Date(date))
            
            val safeNumber = number ?: "Unknown"
            val safeDuration = duration ?: "0"
            
            writer.append("$timeString,$safeNumber,$type,$safeDuration\n")
            writer.flush()
            writer.close()
            
            Log.d(TAG, "Successfully logged call to $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging call to CSV: ${e.message}")
        }
    }
}
