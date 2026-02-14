package com.codetutor.varientdemo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codetutor.varientdemo.diagnostics.DiagnosticsProvider
import com.codetutor.varientdemo.ui.theme.VarientDemoApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VarientDemoApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val appLabel = context.applicationInfo.loadLabel(context.packageManager).toString()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = appLabel,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        ConfigScreen(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ConfigScreen(modifier: Modifier = Modifier){
    val scrollState = rememberScrollState()
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start) {

            if(BuildConfig.DEBUG){
                AssistChip()
            }

            Text(
                text = "Varients Demo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(20.dp),
                fontWeight = FontWeight.SemiBold
            )

            KeyValue("APPLICATION_ID", BuildConfig.APPLICATION_ID)
            KeyValue("VERSION_NAME", BuildConfig.VERSION_NAME ?: "—")
            KeyValue("DEBUG flag", BuildConfig.DEBUG.toString())

            // New for flavors
            KeyValue("FLAVOR (env)", BuildConfig.FLAVOR.ifBlank { "—" })
            KeyValue("BUILD_TYPE", BuildConfig.BUILD_TYPE)
            KeyValue("BASE_URL", BuildConfig.BASE_URL)

            //KeyValue("TIER_NAME", BuildConfig.TIER_NAME)
            KeyValue("SHOW_ADS", BuildConfig.SHOW_ADS.toString())

            if (BuildConfig.SHOW_ADS) {
                Surface(tonalElevation = 2.dp) {
                    Text("[Ad placeholder] — visible in FREE", Modifier.padding(12.dp))
                }
            }

            if (BuildConfig.DEBUG) DebugBanner()
            Text("Diagnostics", style = MaterialTheme.typography.titleMedium)
            DiagnosticsCard()

        }
    }
}

@Composable
private fun KeyValue(key: String, value: String) {
    Column {
        Text(key, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AssistChip() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "DEBUG BUILD",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DiagnosticsCard() {
    val info = DiagnosticsProvider.get().info()
    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
        Text(
            text = info,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DebugBanner() {
    val activity = LocalContext.current as Activity
    Spacer(Modifier.height(8.dp))
    Button (onClick = { ActivityLeaker.leak(activity = activity as MainActivity) }) {
        Text("Trigger Activity Leak")
    }
    Text(
        "Tap the button, then leave this screen (Back). LeakCanary will report a leaked Activity.",
        style = MaterialTheme.typography.bodySmall
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VarientDemoApplicationTheme {
        Greeting("Android")
    }
}