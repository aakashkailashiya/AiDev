package com.ak.aidev

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ak.aidev.ui.composables.CategoryCard
import com.ak.aidev.ui.composables.InfoCardContainer
import com.ak.aidev.ui.composables.InfoRow
import com.ak.aidev.ui.theme.AiDevTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date

class HardwareActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf("main") }
                var title by remember { mutableStateOf("Hardware Information") }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(title) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (currentScreen == "main") {
                                        finish()
                                    } else {
                                        currentScreen = "main"
                                        title = "Hardware Information"
                                    }
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            "main" -> HardwareInfoMainScreen(
                                onCategoryClick = { screen, newTitle ->
                                    if (screen == "apk_extractor") {
                                        context.startActivity(Intent(context, ApkExtractorActivity::class.java))
                                    } else {
                                        currentScreen = screen
                                        title = newTitle
                                    }
                                }
                            )
                            "screen" -> ScreenInfoScreen()
                            "cpu" -> CpuInfoScreen()
                            "memory" -> MemoryInfoScreen()
                            "battery" -> BatteryInfoScreen()
                            "build" -> BuildDetailsScreen()
                            "resources" -> ResourceQualifiersScreen()
                            "system" -> SystemPropertiesScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareInfoMainScreen(onCategoryClick: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryCard("Screen", Icons.Default.Smartphone) { onCategoryClick("screen", "Screen Information") }
        }
        item {
            CategoryCard("CPU", Icons.Default.Memory) { onCategoryClick("cpu", "CPU Information") }
        }
        item {
            CategoryCard("Memory (RAM)", Icons.Default.SdStorage) { onCategoryClick("memory", "Memory Information") }
        }
        item {
            CategoryCard("Battery", Icons.Default.BatteryFull) { onCategoryClick("battery", "Battery Information") }
        }
        item {
            CategoryCard("App Analyzer", Icons.Default.Search) { onCategoryClick("apk_extractor", "App Analyzer") }
        }
        item {
            CategoryCard("Build Details", Icons.Default.Info) { onCategoryClick("build", "Build Details") }
        }
        item {
            CategoryCard("Resource Qualifiers", Icons.Default.SystemUpdate) { onCategoryClick("resources", "Resource Qualifiers") }
        }
        item {
            CategoryCard("System Properties", Icons.Default.Settings) { onCategoryClick("system", "System Properties") }
        }
    }
}

@Composable
fun ScreenInfoScreen() {
    val config = LocalConfiguration.current
    InfoCardContainer {
        InfoRow("Resolution (dp)", "${config.screenWidthDp} x ${config.screenHeightDp} dp")
        InfoRow("Resolution (px)", "${(config.screenWidthDp * config.densityDpi / 160f).toInt()} x ${(config.screenHeightDp * config.densityDpi / 160f).toInt()} px")
        InfoRow("Density", "${config.densityDpi} dpi")
    }
}

@Composable
fun CpuInfoScreen() {
    val cpuInfo = remember { getCpuInfo() }
    InfoCardContainer {
        cpuInfo.forEach { (key, value) ->
            InfoRow(key, value)
        }
        InfoRow("ABI", Build.SUPPORTED_ABIS.joinToString())
    }
}

@Composable
fun MemoryInfoScreen() {
    val context = LocalContext.current
    val memoryInfo = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mi)
        mi
    }
    val totalMemory = memoryInfo.totalMem / (1024 * 1024)
    val availableMemory = memoryInfo.availMem / (1024 * 1024)

    InfoCardContainer {
        InfoRow("Total Memory", "${totalMemory} MB")
        InfoRow("Available Memory", "${availableMemory} MB")
    }
}

@Composable
fun BatteryInfoScreen() {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf<Int?>(null) }
    var batteryStatus by remember { mutableStateOf<String?>(null) }
    var batteryHealth by remember { mutableStateOf<String?>(null) }
    var batteryTemperature by remember { mutableStateOf<Float?>(null) }
    var batteryVoltage by remember { mutableStateOf<Int?>(null) }

    val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else null

                val statusInt = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                batteryStatus = when (statusInt) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                    BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                    else -> null
                }

                val healthInt = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                batteryHealth = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                    else -> null
                }
                batteryTemperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
                batteryVoltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    InfoCardContainer {
        batteryLevel?.let { InfoRow("Level", "$it%") }
        batteryStatus?.let { InfoRow("Status", it) }
        batteryHealth?.let { InfoRow("Health", it) }
        batteryTemperature?.let { InfoRow("Temperature", "$it °C") }
        batteryVoltage?.let { InfoRow("Voltage", "$it mV") }
    }
}

@Composable
fun BuildDetailsScreen() {
    InfoCardContainer {
        InfoRow("Brand", Build.BRAND)
        InfoRow("Model", Build.MODEL)
        InfoRow("Device", Build.DEVICE)
        InfoRow("Product", Build.PRODUCT)
        InfoRow("Manufacturer", Build.MANUFACTURER)
        InfoRow("Board", Build.BOARD)
        InfoRow("Hardware", Build.HARDWARE)
        InfoRow("Android Version", Build.VERSION.RELEASE)
        InfoRow("API Level", Build.VERSION.SDK_INT.toString())
        InfoRow("Build ID", Build.DISPLAY)
        InfoRow("Build Time", SimpleDateFormat.getDateTimeInstance().format(Date(Build.TIME)))
    }
}

@Composable
fun ResourceQualifiersScreen() {
    val config = LocalConfiguration.current
    InfoCardContainer {
        InfoRow("MCC", config.mcc.toString())
        InfoRow("MNC", config.mnc.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            InfoRow("Locale", config.locales[0].toLanguageTag())
        }
        InfoRow("Layout Direction", if (config.layoutDirection == 0) "LTR" else "RTL")
        InfoRow("Smallest Screen Width", "${config.smallestScreenWidthDp} dp")
        InfoRow("Screen Width", "${config.screenWidthDp} dp")
        InfoRow("Screen Height", "${config.screenHeightDp} dp")
        InfoRow("Screen Layout", getScreenLayoutString(config.screenLayout))
        InfoRow("UI Mode", getUiModeString(config.uiMode))
        InfoRow("Night Mode", if (config.isNightModeActive) "Yes" else "No")
    }
}

private fun getScreenLayoutString(screenLayout: Int): String {
    val size = screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
    val long = screenLayout and android.content.res.Configuration.SCREENLAYOUT_LONG_MASK
    val sizeStr = when (size) {
        android.content.res.Configuration.SCREENLAYOUT_SIZE_SMALL -> "small"
        android.content.res.Configuration.SCREENLAYOUT_SIZE_NORMAL -> "normal"
        android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE -> "large"
        android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE -> "xlarge"
        else -> "undefined"
    }
    val longStr = when (long) {
        android.content.res.Configuration.SCREENLAYOUT_LONG_YES -> "long"
        android.content.res.Configuration.SCREENLAYOUT_LONG_NO -> "notlong"
        else -> "undefined"
    }
    return "$sizeStr, $longStr"
}

private fun getUiModeString(uiMode: Int): String {
    val type = uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK
    return when (type) {
        android.content.res.Configuration.UI_MODE_TYPE_NORMAL -> "normal"
        android.content.res.Configuration.UI_MODE_TYPE_DESK -> "desk"
        android.content.res.Configuration.UI_MODE_TYPE_CAR -> "car"
        android.content.res.Configuration.UI_MODE_TYPE_TELEVISION -> "television"
        android.content.res.Configuration.UI_MODE_TYPE_APPLIANCE -> "appliance"
        android.content.res.Configuration.UI_MODE_TYPE_WATCH -> "watch"
        else -> "undefined"
    }
}

@Composable
fun SystemPropertiesScreen() {
    val properties = remember { System.getProperties().map { it.key.toString() to it.value.toString() }.sortedBy { it.first } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(properties) { (key, value) ->
            InfoRow(key, value)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

private fun getCpuInfo(): Map<String, String> {
    val info = mutableMapOf<String, String>()
    try {
        val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line?.split(":")?.map { it.trim() }
            if (parts != null && parts.size > 1) {
                val key = parts[0]
                val value = parts[1]
                if (key.equals("Processor", ignoreCase = true) || key.equals("Hardware", ignoreCase = true) || key.equals("model name", ignoreCase = true)) {
                    if (!info.containsKey(key)) {
                        info[key] = value
                    }
                }
            }
        }
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return info
}
