package com.ak.aidev

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ak.aidev.ui.composables.CategoryCard
import com.ak.aidev.ui.composables.InfoRow
import com.ak.aidev.ui.theme.AiDevTheme

class SensorsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                var currentScreen by remember { mutableStateOf("main") }
                var title by remember { mutableStateOf("Sensors") }

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
                                        title = "Sensors"
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
                            "main" -> SensorsMainScreen { screen, newTitle ->
                                currentScreen = screen
                                title = newTitle
                            }
                            "motion" -> SensorListScreen(
                                Sensor.TYPE_ACCELEROMETER,
                                Sensor.TYPE_GYROSCOPE,
                                Sensor.TYPE_GRAVITY,
                                Sensor.TYPE_LINEAR_ACCELERATION,
                                Sensor.TYPE_ROTATION_VECTOR
                            )
                            "position" -> SensorListScreen(
                                Sensor.TYPE_MAGNETIC_FIELD,
                                Sensor.TYPE_PROXIMITY,
                                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
                            )
                            "environment" -> SensorListScreen(
                                Sensor.TYPE_LIGHT,
                                Sensor.TYPE_PRESSURE,
                                Sensor.TYPE_AMBIENT_TEMPERATURE,
                                Sensor.TYPE_RELATIVE_HUMIDITY
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorsMainScreen(onCategoryClick: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryCard("Motion Sensors", Icons.Default.DirectionsRun) { onCategoryClick("motion", "Motion Sensors") }
        }
        item {
            CategoryCard("Position Sensors", Icons.Default.LocationOn) { onCategoryClick("position", "Position Sensors") }
        }
        item {
            CategoryCard("Environment Sensors", Icons.Default.WbSunny) { onCategoryClick("environment", "Environment Sensors") }
        }
    }
}

@Composable
fun SensorListScreen(vararg sensorTypes: Int) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensors = remember {
        sensorTypes.flatMap { sensorType ->
            sensorManager.getSensorList(sensorType)
        }.distinctBy { it.name }
    }

    if (sensors.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No sensors of this type found on this device.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sensors) { sensor ->
                SensorInfoCard(sensor)
            }
        }
    }
}

@Composable
fun SensorInfoCard(sensor: Sensor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(sensor.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Vendor", sensor.vendor)
            InfoRow("Version", sensor.version.toString())
            InfoRow("Power (mA)", sensor.power.toString())
            InfoRow("Resolution", sensor.resolution.toString())
            InfoRow("Max Range", sensor.maximumRange.toString())
        }
    }
}
