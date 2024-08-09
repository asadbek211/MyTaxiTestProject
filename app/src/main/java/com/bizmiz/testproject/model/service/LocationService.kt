package com.bizmiz.testproject.model.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bizmiz.testproject.R
import com.bizmiz.testproject.data.db.LocationModel
import com.bizmiz.testproject.model.repository.LocationRepository
import com.bizmiz.testproject.model.service.location.DefaultLocationClient
import com.bizmiz.testproject.model.service.location.LocationClient
import com.bizmiz.testproject.util.Constants.CHANNEL_ID
import com.bizmiz.testproject.util.Constants.CHANNEL_NAME
import com.bizmiz.testproject.util.Constants.DEFAULT_INTERVAL_IN_MILLISECONDS
import com.bizmiz.testproject.util.Constants.NOTIFICATION_TITLE
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject


class LocationService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var locationClient: LocationClient
    private val locationRepository: LocationRepository by inject()
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, createPersistentNotification().build())
        startLocationUpdates()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPersistentNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(NOTIFICATION_TITLE)
            .setContentText("App is running...").setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX).setAutoCancel(false).setOngoing(true)
    }

    private fun startLocationUpdates() {
        locationClient = DefaultLocationClient(
            this, LocationServices.getFusedLocationProviderClient(this)
        )
        locationClient.getLocationUpdates(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.d("@@@", "${location.latitude}, ${location.longitude}")
                locationRepository.addLocation(
                    LocationModel(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        time = System.currentTimeMillis()
                    )
                )
            }.launchIn(scope)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scope.cancel()
        stopSelf()
        super.onTaskRemoved(rootIntent)

    }
    override fun onBind(intent: Intent?): IBinder? = null
}