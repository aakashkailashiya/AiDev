package com.ak.aidev

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ak.aidev.ui.composables.CategoryCard
import com.ak.aidev.ui.composables.InfoCardContainer
import com.ak.aidev.ui.composables.InfoRow
import com.ak.aidev.ui.theme.AiDevTheme

class ConnectivityActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.READ_PHONE_STATE
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { recreate() }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionLauncher.launch(permissions)
        }

        setContent {
            AiDevTheme {
                var currentScreen by remember { mutableStateOf("main") }
                var title by remember { mutableStateOf("Connectivity") }

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
                                        title = "Connectivity"
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
                            "main" -> ConnectivityMainScreen { screen, newTitle ->
                                currentScreen = screen
                                title = newTitle
                            }
                            "wifi" -> WifiDetailsScreen()
                            "cellular" -> CellularDetailsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectivityMainScreen(onCategoryClick: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryCard("Wi-Fi Details", Icons.Default.Wifi) { onCategoryClick("wifi", "Wi-Fi Details") }
        }
        item {
            CategoryCard("Cellular Details", Icons.Default.NetworkCell) { onCategoryClick("cellular", "Cellular Details") }
        }
    }
}

@Composable
fun WifiDetailsScreen() {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val connectionInfo = wifiManager.connectionInfo
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    InfoCardContainer {
        InfoRow("SSID", connectionInfo.ssid.removeSurrounding("\""))
        InfoRow("BSSID", connectionInfo.bssid)
        InfoRow("IP Address", intToIp(connectionInfo.ipAddress))
        InfoRow("Link Speed", "${connectionInfo.linkSpeed} Mbps")
        InfoRow("Signal Strength (RSSI)", "${connectionInfo.rssi} dBm")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InfoRow("MAC Address", connectionInfo.macAddress)
        }
        capabilities?.let {
            InfoRow("Downstream", "${it.linkDownstreamBandwidthKbps / 1000} Mbps")
            InfoRow("Upstream", "${it.linkUpstreamBandwidthKbps / 1000} Mbps")
        }
    }
}

@Composable
fun CellularDetailsScreen() {
    val context = LocalContext.current
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Permission not granted.")
        }
        return
    }

    InfoCardContainer {
        InfoRow("Network Operator", telephonyManager.networkOperatorName)
        InfoRow("Network Type", getNetworkType(telephonyManager.dataNetworkType))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            telephonyManager.signalStrength?.let {
                it.cellSignalStrengths.forEach { cell ->
                    InfoRow("Signal Strength", "${cell.dbm} dBm")
                }
            }
        }
    }
}

private fun intToIp(ip: Int): String {
    return (ip and 0xFF).toString() + "." +
            (ip shr 8 and 0xFF) + "." +
            (ip shr 16 and 0xFF) + "." +
            (ip shr 24 and 0xFF)
}

private fun getNetworkType(networkType: Int): String {
    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        else -> "Unknown"
    }
}
