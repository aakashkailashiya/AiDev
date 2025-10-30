package com.ak.aidev

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ak.aidev.ui.theme.AiDevTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiDevTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ModernSettingsPanel(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class SettingsButtonInfo(
    val text: String,
    val intentAction: String,
    val color: Color,
    val icon: ImageVector
)

data class SettingsCategory(
    val title: String,
    val buttons: List<SettingsButtonInfo>
)

sealed interface SettingsListItem
data class HeaderItem(val title: String) : SettingsListItem
data class ButtonItem(val info: SettingsButtonInfo) : SettingsListItem

@Composable
fun ModernSettingsPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val categories = remember {
        listOf(
            SettingsCategory(
                "Device & Hardware",
                listOf(
                    SettingsButtonInfo("Display", Settings.ACTION_DISPLAY_SETTINGS, Color(0xFFFF9800), Icons.Default.Smartphone),
                    SettingsButtonInfo("Sound", Settings.ACTION_SOUND_SETTINGS, Color(0xFF9C27B0), Icons.Default.VolumeUp),
                    SettingsButtonInfo("Network", Settings.ACTION_WIRELESS_SETTINGS, Color(0xFF009688), Icons.Default.NetworkCell),
                    SettingsButtonInfo("Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS, Color(0xFF3F51B5), Icons.Default.Bluetooth),
                    SettingsButtonInfo("Location", Settings.ACTION_LOCATION_SOURCE_SETTINGS, Color(0xFFE91E63), Icons.Default.LocationOn),
                    SettingsButtonInfo("About Phone", Settings.ACTION_DEVICE_INFO_SETTINGS, Color(0xFF795548), Icons.Default.Info),
                    SettingsButtonInfo("Hardware", "HARDWARE_ACTIVITY", Color(0xFF607D8B), Icons.Default.Memory),
                    SettingsButtonInfo("Sensors", "SENSORS_ACTIVITY", Color(0xFF795548), Icons.Default.Sensors)
                )
            ),
            SettingsCategory(
                "Developer Tools",
                listOf(
                    SettingsButtonInfo("Developer Options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, Color(0xFF4CAF50), Icons.Default.Build),
                    SettingsButtonInfo("Wireless Debugging", "android.settings.WIRELESS_DEBUGGING_SETTINGS", Color(0xFF2196F3), Icons.Default.Wifi),
                    SettingsButtonInfo("Software", "APK_EXTRACTOR_ACTIVITY", Color(0xFF8BC34A), Icons.Default.Apps),
                    SettingsButtonInfo("Developer Tools", "DEV_TOOLS_ACTIVITY", Color(0xFF00BCD4), Icons.Default.BuildCircle),
                    SettingsButtonInfo("Connectivity", "CONNECTIVITY_ACTIVITY", Color(0xFF03A9F4), Icons.Default.SignalCellularAlt)
                )
            ),
            SettingsCategory(
                "Productivity",
                listOf(
                    SettingsButtonInfo("Pomodoro Clock", "POMODORO_ACTIVITY", Color(0xFFFF5722), Icons.Default.Timer),
                    SettingsButtonInfo("Power Button", "APP_SETTINGS_ACTIVITY", Color(0xFFE91E63), Icons.Default.PowerSettingsNew)
                )
            )
        )
    }

    val settingsItems = remember(categories) {
        buildList {
            categories.forEach { category ->
                add(HeaderItem(category.title))
                category.buttons.forEach { button ->
                    add(ButtonItem(button))
                }
            }
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    var ramUsage by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var storageUsage by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var cpuUsage by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            withContext(Dispatchers.IO) {
                // RAM
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)
                val totalRam = memInfo.totalMem
                val usedRam = totalRam - memInfo.availMem
                ramUsage = usedRam to totalRam

                // Storage
                val internalStatFs = StatFs(Environment.getDataDirectory().path)
                val totalInternal = internalStatFs.blockCountLong * internalStatFs.blockSizeLong
                val availableInternal = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong
                storageUsage = (totalInternal - availableInternal) to totalInternal

                // CPU
                cpuUsage = getCpuUsage()
            }
            delay(2000) // Refresh every 2 seconds
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            LiveDashboard(cpuUsage, ramUsage, storageUsage)
        }

        itemsIndexed(
            items = settingsItems,
            key = { _, item ->
                when (item) {
                    is HeaderItem -> item.title
                    is ButtonItem -> item.info.intentAction
                }
            },
            span = { _, item ->
                when (item) {
                    is HeaderItem -> GridItemSpan(2)
                    is ButtonItem -> GridItemSpan(1)
                }
            }
        ) { index, item ->
            when (item) {
                is HeaderItem -> {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(delayMillis = index * 25))
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }
                }
                is ButtonItem -> {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(delayMillis = index * 25)) + scaleIn(animationSpec = tween(delayMillis = index * 25), initialScale = 0.8f)
                    ) {
                        SettingsButton(info = item.info, onClick = {
                            try {
                                val intent = when (item.info.intentAction) {
                                    "POMODORO_ACTIVITY" -> Intent(context, PomodoroActivity::class.java)
                                    "APK_EXTRACTOR_ACTIVITY" -> Intent(context, ApkExtractorActivity::class.java)
                                    "HARDWARE_ACTIVITY" -> Intent(context, HardwareActivity::class.java)
                                    "SENSORS_ACTIVITY" -> Intent(context, SensorsActivity::class.java)
                                    "CONNECTIVITY_ACTIVITY" -> Intent(context, ConnectivityActivity::class.java)
                                    "DEV_TOOLS_ACTIVITY" -> Intent(context, DevToolsActivity::class.java)
                                    "APP_SETTINGS_ACTIVITY" -> Intent(context, AppSettingsActivity::class.java)
                                    else -> Intent(item.info.intentAction)
                                }
                                intent?.let { context.startActivity(it) }
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, "${item.info.text} not available", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveDashboard(
    cpuUsage: Float?,
    ramUsage: Pair<Long, Long>?,
    storageUsage: Pair<Long, Long>?
) {
    Column {
        Text(
            text = "Live Dashboard",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                DashboardCard(
                    title = "CPU Usage",
                    icon = Icons.Default.Speed,
                    usage = cpuUsage ?: 0f,
                    formattedText = cpuUsage?.let { "${DecimalFormat("#.##").format(it)}%" } ?: "Not available"
                )
            }
            item {
                ramUsage?.let {
                    DashboardCard(
                        title = "RAM Usage",
                        icon = Icons.Default.Memory,
                        usage = (it.first.toFloat() / it.second.toFloat()) * 100,
                        formattedText = "${formatSize(it.first)} / ${formatSize(it.second)}"
                    )
                }
            }
            item { BatteryCard() }
            item {
                storageUsage?.let {
                    DashboardCard(
                        title = "Internal Storage",
                        icon = Icons.Default.Save,
                        usage = (it.first.toFloat() / it.second.toFloat()) * 100,
                        formattedText = "${formatSize(it.first)} / ${formatSize(it.second)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsButton(info: SettingsButtonInfo, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Button(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .height(120.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = info.color),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.text,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = info.text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashboardCard(title: String, icon: ImageVector, usage: Float, formattedText: String) {
    Card(modifier = Modifier.width(200.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { usage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(formattedText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun BatteryCard() {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf<Int?>(null) }
    var batteryStatus by remember { mutableStateOf<String?>(null) }

    val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else null

                val statusInt = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                batteryStatus = when (statusInt) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    else -> "Discharging"
                }
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose { context.unregisterReceiver(batteryReceiver) }
    }

    batteryLevel?.let {
        DashboardCard(
            title = "Battery",
            icon = Icons.Default.BatteryFull,
            usage = it.toFloat(),
            formattedText = "$it% ($batteryStatus)"
        )
    }
}

private suspend fun getCpuUsage(): Float? {
    return withContext(Dispatchers.IO) {
        try {
            val reader1 = RandomAccessFile("/proc/stat", "r")
            val load1 = reader1.readLine().split(" ").filter { it.isNotBlank() }
            reader1.close()

            val idle1 = load1[4].toLong()
            val total1 = load1.subList(1, load1.size).sumOf { it.toLong() }

            delay(500)

            val reader2 = RandomAccessFile("/proc/stat", "r")
            val load2 = reader2.readLine().split(" ").filter { it.isNotBlank() }
            reader2.close()

            val idle2 = load2[4].toLong()
            val total2 = load2.subList(1, load2.size).sumOf { it.toLong() }

            val totalDiff = total2 - total1
            val idleDiff = idle2 - idle1

            if (totalDiff > 0) {
                (100.0f * (totalDiff - idleDiff) / totalDiff)
            } else {
                0.0f
            }
        } catch (ex: Exception) {
            null
        }
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

@Preview(showBackground = true)
@Composable
fun ModernSettingsPanelPreview() {
    AiDevTheme {
        ModernSettingsPanel()
    }
}
