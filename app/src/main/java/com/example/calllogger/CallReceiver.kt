package com.example.calllogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import java.util.concurrent.Executors

class CallReceiver : BroadcastReceiver() {
    private val TAG = "CallReceiver"
    private val executor = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                // Call ended. Use goAsync to allow background work
                val pendingResult = goAsync()
                executor.execute {
                    try {
                        // Wait a bit for the system to write to CallLog
                        Thread.sleep(2000)
                        logRecentCall(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing call log", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun logRecentCall(context: Context) {
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC limit 1"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                    val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                    val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                    val number = if (numberIndex != -1) it.getString(numberIndex) else "Unknown"
                    val typeStr = if (typeIndex != -1) it.getString(typeIndex) else "0"
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                    val duration = if (durationIndex != -1) it.getString(durationIndex) else "0"

                    val prefs = context.getSharedPreferences("CallLoggerPrefs", Context.MODE_PRIVATE)
                    val lastLoggedDate = prefs.getLong("last_logged_date", 0)

                    if (date > lastLoggedDate) {
                        val type = when (typeStr.toIntOrNull() ?: 0) {
                            CallLog.Calls.INCOMING_TYPE -> "Incoming"
                            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                            CallLog.Calls.MISSED_TYPE -> "Missed"
                            CallLog.Calls.REJECTED_TYPE -> "Rejected"
                            else -> "Unknown ($typeStr)"
                        }

                        CsvLogger.logCall(context, number, type, duration, date)
                        prefs.edit().putLong("last_logged_date", date).apply()
                    } else {
                        Log.d(TAG, "Call already logged or no new calls.")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission to read CallLog", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading CallLog", e)
        }
    }
}
