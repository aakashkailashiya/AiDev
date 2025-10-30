package com.ak.aidev

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ak.aidev.services.PowerButtonService
import com.ak.aidev.ui.theme.AiDevTheme

class AppSettingsActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("App Settings") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        PowerButtonToggle { requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerButtonToggle(requestPermission: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var isEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("power_button_listener_enabled", false)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Enable Power Button Logging")
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission()
                } else {
                    isEnabled = it
                    sharedPrefs.edit().putBoolean("power_button_listener_enabled", it).apply()
                    if (it) {
                        Intent(context, PowerButtonService::class.java).also {
                            context.startService(it)
                        }
                    } else {
                        context.stopService(Intent(context, PowerButtonService::class.java))
                    }
                }
            }
        )
    }
}
