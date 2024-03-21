package com.example.synopticmobile
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class ChargingLogActivity : AppCompatActivity() {
    private val chargingEvents = mutableListOf<ChargingEvent>()
    private lateinit var adapter: ChargingLogAdapter
//    private val ACTION_SHOW_CHARGING_LOG = "com.example.synopticmobile.SHOW_CHARGING_LOG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.charging_log_layout)

        loadChargingEvents()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ChargingLogAdapter(chargingEvents)
        recyclerView.adapter = adapter

        registerChargerConnectionReceiver()

    }



    private fun saveChargingEvents() {
        val gson = Gson()
        val json = gson.toJson(chargingEvents)
        val prefs = getSharedPreferences("ChargingEvents", Context.MODE_PRIVATE)
        prefs.edit().putString("events", json).apply()
    }


    private fun loadChargingEvents() {
        val prefs = getSharedPreferences("ChargingEvents", Context.MODE_PRIVATE)
        val json = prefs.getString("events", "")
        if (!json.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<List<ChargingEvent>>() {}.type
            val eventsList = gson.fromJson<List<ChargingEvent>>(json, type)
            chargingEvents.addAll(eventsList)
            adapter.notifyDataSetChanged()
        }
    }


    private fun addChargingEvent(time: Long, eventType: String) {
        val newEvent = ChargingEvent(time, eventType)
        chargingEvents.add(newEvent)
        adapter.notifyDataSetChanged()
        saveChargingEvents()
    }


    private val chargerConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == Intent.ACTION_POWER_CONNECTED) {

                    val newEvent = ChargingEvent(System.currentTimeMillis(), "Charger connected")
                    addChargingEvent(newEvent.time, newEvent.eventType)
                }
            }
        }
    }

    private fun registerChargerConnectionReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
        }
        registerReceiver(chargerConnectionReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(chargerConnectionReceiver)
    }
}