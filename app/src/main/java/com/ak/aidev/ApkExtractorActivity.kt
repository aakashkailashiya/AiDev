package com.ak.aidev

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ak.aidev.ui.theme.AiDevTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkExtractorActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiDevTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.AppList) }
                var title by remember { mutableStateOf("App Analyzer") }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(title) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (currentScreen is Screen.AppDetails) {
                                        currentScreen = Screen.AppList
                                        title = "App Analyzer"
                                    } else {
                                        finish()
                                    }
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (val screen = currentScreen) {
                            is Screen.AppList -> AppListScreen {
                                currentScreen = Screen.AppDetails(it)
                                title = it.loadLabel(packageManager).toString()
                            }
                            is Screen.AppDetails -> AppDetailsScreen(screen.appInfo)
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object AppList : Screen()
    data class AppDetails(val appInfo: ApplicationInfo) : Screen()
}

@Composable
fun AppListScreen(onAppClick: (ApplicationInfo) -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<ApplicationInfo>?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { it.loadLabel(pm).toString() }
        }
    }

    if (apps == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(apps!!) { app ->
                AppListItem(app) { onAppClick(app) }
            }
        }
    }
}

@Composable
fun AppListItem(appInfo: ApplicationInfo, onClick: () -> Unit) {
    val pm = LocalContext.current.packageManager
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = appInfo.loadIcon(pm),
                contentDescription = "App Icon",
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(appInfo.loadLabel(pm).toString(), fontWeight = FontWeight.Bold)
                Text(appInfo.packageName, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun AppDetailsScreen(appInfo: ApplicationInfo) {
    val context = LocalContext.current
    val pm = context.packageManager
    val packageInfo = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(appInfo.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS)
        }
    }

    var showPermissions by remember { mutableStateOf(false) }
    var showManifest by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Permissions", modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Permissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showPermissions = !showPermissions }) {
                        Text(if (showPermissions) "Hide" else "Show")
                    }
                    if (showPermissions) {
                        packageInfo.requestedPermissions?.forEach {
                            Text(it)
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = "Manifest", modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Android Manifest", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showManifest = !showManifest }) {
                        Text(if (showManifest) "Hide" else "Show")
                    }
                    if (showManifest) {
                        Text("Manifest viewing is under development.")
                    }
                }
            }
        }
    }
}
