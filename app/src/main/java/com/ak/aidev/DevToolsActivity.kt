package com.ak.aidev

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ak.aidev.ui.composables.CategoryCard
import com.ak.aidev.ui.composables.InfoCardContainer
import com.ak.aidev.ui.composables.InfoRow
import com.ak.aidev.ui.theme.AiDevTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DevToolsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                var currentScreen by remember { mutableStateOf("main") }
                var title by remember { mutableStateOf("Developer Tools") }

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
                                        title = "Developer Tools"
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
                            "main" -> DevToolsMainScreen { screen, newTitle ->
                                currentScreen = screen
                                title = newTitle
                            }
                            "logcat" -> LogcatScreen()
                            "codecs" -> MediaCodecsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevToolsMainScreen(onCategoryClick: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryCard("Logcat Viewer", Icons.Default.Build) { onCategoryClick("logcat", "Logcat Viewer") }
        }
        item {
            CategoryCard("Media Codecs", Icons.Default.Info) { onCategoryClick("codecs", "Media Codecs") }
        }
    }
}

@Composable
fun LogcatScreen() {
    var logcat by remember { mutableStateOf("Loading logs...") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec("logcat -d")
                val bufferedReader = process.inputStream.bufferedReader()
                val log = bufferedReader.readText()
                logcat = log
            } catch (e: Exception) {
                logcat = "Failed to load logcat: ${e.message}"
            }
        }
    }

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        item {
            Text(logcat)
        }
    }
}

@Composable
fun MediaCodecsScreen() {
    val codecs = remember {
        MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.map { it.name }
    }

    InfoCardContainer {
        codecs.forEach { codec ->
            InfoRow("Codec", codec)
        }
    }
}
