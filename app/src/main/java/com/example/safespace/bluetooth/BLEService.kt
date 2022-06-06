package com.example.safespace.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safespace.R
import com.example.safespace.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Time
import java.time.LocalDateTime
import java.util.*

class BLEService : Service() {



    var TAG = "Bluetooth Connection"

    private lateinit var m_BluetoothAdapter: BluetoothAdapter
    private lateinit var m_BluetoothGat: BluetoothGatt
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Represents a geographical location.
     */
    protected var mLastLocation: Location? = null
    private var mLatitude: Double? = null
    private var mLongitude: Double? = null


    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        connectToBluetoothDevice()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation.addOnCompleteListener { task : Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result
                    mLatitude = mLastLocation!!.latitude
                    mLongitude = mLastLocation!!.longitude
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    fun connectToBluetoothDevice() {
        val bluetoothManager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        m_BluetoothAdapter = bluetoothManager.adapter
        for (dt in m_BluetoothAdapter.bondedDevices) {
            if (dt.name.lowercase() == "safespace") {
                val device = m_BluetoothAdapter.getRemoteDevice(dt.address)
                m_BluetoothGat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    device.connectGatt(
                        applicationContext,
                        true, // was false
                        this.m_GattCallBack,
                        2
                    )
                } else
                {
                    return
                }
                Log.d("Result", (m_BluetoothGat.connect()).toString())
            }
        }
    }

    private val m_GattCallBack = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "DEVICE CONNECTED. DISCOVERING SERVICES...")
                m_BluetoothGat.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "DEVICE DISCONNECTED")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "SERVICES DISCOVERED. PARSING...")
                if (gatt != null) {
                    displayGattServices(gatt.services)
                }
            } else {
                Log.i(TAG, "FAILED TO DISCOVER SERVICES")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "ON CHARACTERISTIC READ SUCCESSFUL")
            } else {
                Log.i(TAG, "ERROR READING CHARACTERISTIC")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "ON CHARACTERISTIC WRITE SUCCESSFUL")
            } else {
                Log.i(TAG, "ERROR WRITING CHARACTERISTIC")
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i(TAG, "NEW NOTIFICATION RECEIVED")

            getLastLocation()
            ////////// PUT IN DIFFERENT CLASS ///////////
            val database : FirebaseFirestore = FirebaseFirestore.getInstance()
            val locationHashMap : HashMap<String, Any> = HashMap()
                val north = mLatitude
                val east = mLongitude
                locationHashMap["latitude"] = north as Double
                locationHashMap["longitude"] = east as Double
                locationHashMap["time"]= LocalDateTime.now()
                database.collection("Alert_places").add(locationHashMap)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Log.i(TAG, "NEW RSSI VALUE RECEIVED")
        }

    }

    @SuppressLint("MissingPermission")
    private fun displayGattServices(gattServices: MutableList<BluetoothGattService>) {

        if (gattServices == null) return
        for (gattService: BluetoothGattService in gattServices) {
            Log.i(TAG, "SERVICE FOUND: " + gattService.uuid.toString())
            for (gattCharacteristic: BluetoothGattCharacteristic in gattService.characteristics) {
                Log.i(
                    TAG,
                    "  CHAR. FOUND: " + gattCharacteristic.uuid.toString()
                )
                if (gattCharacteristic.uuid.toString() == "beb5483e-36e1-4688-b7f5-ea07361b26a8")
                {

                    val uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") // 0x2902
                    m_BluetoothGat.setCharacteristicNotification(gattCharacteristic,true)
                    val descriptor = gattCharacteristic.getDescriptor(uuid)
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    m_BluetoothGat.writeDescriptor(descriptor)
                    Log.d("displayGattServices","Subscribing to characteristic!")
                }
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                ""
            }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("title")
            .setContentText("text")
            .build()
        startForeground(2001, notification)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}