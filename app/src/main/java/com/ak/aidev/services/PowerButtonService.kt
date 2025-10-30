package com.ak.aidev.services

import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.StatFs
import android.telephony.TelephonyManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PowerButtonService : Service() {

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                logSystemInfo(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun logSystemInfo(context: Context) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val logFileName = "SystemInfo_$timestamp.txt"
        val logDir = context.getExternalFilesDir("logs")
        if (logDir != null) {
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val logFile = File(logDir, logFileName)

            val info = buildString {
                appendLine("System Info Dump at ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("====================")

                // Build Info
                appendLine("\n--- BUILD INFO ---")
                appendLine("Brand: ${Build.BRAND}")
                appendLine("Model: ${Build.MODEL}")
                appendLine("Device: ${Build.DEVICE}")
                appendLine("Product: ${Build.PRODUCT}")
                appendLine("Manufacturer: ${Build.MANUFACTURER}")
                appendLine("Board: ${Build.BOARD}")
                appendLine("Hardware: ${Build.HARDWARE}")
                appendLine("Android Version: ${Build.VERSION.RELEASE}")
                appendLine("API Level: ${Build.VERSION.SDK_INT}")
                appendLine("Build ID: ${Build.DISPLAY}")
                appendLine("Build Time: ${Date(Build.TIME)}")
                appendLine("ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")

                // CPU Info
                appendLine("\n--- CPU INFO ---")
                try {
                    val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
                    process.inputStream.bufferedReader().use {
                        it.forEachLine { line -> appendLine(line) }
                    }
                } catch (ex: Exception) {
                    appendLine("Could not read /proc/cpuinfo: ${ex.message}")
                }

                // Memory Info
                appendLine("\n--- MEMORY (RAM) ---")
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)
                appendLine("Total Memory: ${formatSize(memInfo.totalMem)}")
                appendLine("Available Memory: ${formatSize(memInfo.availMem)}")
                appendLine("Low Memory: ${memInfo.lowMemory}")

                // Storage Info
                appendLine("\n--- STORAGE (INTERNAL) ---")
                val internalStatFs = StatFs(Environment.getDataDirectory().path)
                val totalInternal = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
                val availableInternal = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
                appendLine("Total: ${formatSize(totalInternal)}")
                appendLine("Used: ${formatSize(totalInternal - availableInternal)}")
                appendLine("Available: ${formatSize(availableInternal)}")

                // Battery Info
                appendLine("\n--- BATTERY ---")
                val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryIntent = context.registerReceiver(null, iFilter)
                if (batteryIntent != null) {
                    val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()) else -1f
                    appendLine("Level: $batteryPct%")

                    val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    appendLine("Status: ${getBatteryStatus(status)}")

                    val health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    appendLine("Health: ${getBatteryHealth(health)}")

                    val temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
                    appendLine("Temperature: $temp °C")

                    val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    appendLine("Voltage: $voltage mV")

                    val technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                    appendLine("Technology: $technology")

                } else {
                    appendLine("Could not retrieve battery info.")
                }

                // Sensors
                appendLine("\n--- SENSORS ---")
                val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
                if (sensors.isNotEmpty()) {
                    sensors.forEach { sensor ->
                        appendLine("- ${sensor.name} (Vendor: ${sensor.vendor}, Type: ${sensor.stringType})")
                    }
                } else {
                    appendLine("No sensors found.")
                }

                // Connectivity
                appendLine("\n--- CONNECTIVITY ---")
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                appendLine("-- Wi-Fi --")
                try {
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                        val wifiInfo = wifiManager.connectionInfo
                        appendLine("SSID: ${wifiInfo.ssid.removeSurrounding("\"")}")
                        appendLine("BSSID: ${wifiInfo.bssid}")
                        val ip = wifiInfo.ipAddress
                        val ipAddress = (ip and 0xFF).toString() + "." + (ip shr 8 and 0xFF) + "." + (ip shr 16 and 0xFF) + "." + (ip shr 24 and 0xFF)
                        appendLine("IP Address: $ipAddress")
                        appendLine("Link Speed: ${wifiInfo.linkSpeed} Mbps")
                        appendLine("RSSI: ${wifiInfo.rssi} dBm")
                    } else {
                        appendLine("ACCESS_WIFI_STATE permission not granted.")
                    }
                } catch (e: Exception) {
                    appendLine("Error getting Wi-Fi info: ${e.message}")
                }

                appendLine("\n-- Cellular --")
                try {
                    if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        appendLine("Network Operator: ${telephonyManager.networkOperatorName}")
                    } else {
                        appendLine("READ_PHONE_STATE permission not granted.")
                    }
                } catch (e: Exception) {
                    appendLine("Error getting Cellular info: ${e.message}")
                }
            }
            logFile.writeText(info)
        }
    }

    private fun getBatteryStatus(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
            else -> "N/A"
        }
    }

    private fun getBatteryHealth(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
            else -> "N/A"
        }
    }

    private fun formatSize(size: Long): String {
        val kiloByte = 1024.0
        val megaByte = kiloByte * 1024
        val gigaByte = megaByte * 1024

        return when {
            size < megaByte -> "%.2f KB".format(size / kiloByte)
            size < gigaByte -> "%.2f MB".format(size / megaByte)
            else -> "%.2f GB".format(size / gigaByte)
        }
    }
}
