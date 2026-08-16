package com.vitalmind.mobilewear.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.vitalmind.mobilewear.wear.data.WearHomeDataListener
import com.vitalmind.mobilewear.wear.health.HeartRateManager
import com.vitalmind.mobilewear.wear.presentation.navigation.WearNavigation
import com.vitalmind.mobilewear.wear.presentation.theme.VitalMindWearTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            VitalMindWearApp()
        }
    }
}

@Composable
fun VitalMindWearApp() {
    VitalMindWearTheme {
        AppScaffold {
            WearNavigation()
        }
    }
}

private fun translateWearLevel(
    level: String?
): String {
    return when (
        level?.lowercase()
    ) {
        "low" -> "Bajo"
        "medium" -> "Medio"
        "high" -> "Alto"
        else -> "Sin datos"
    }
}

@Composable
fun WearHomeScreen(
    onOpenChat: () -> Unit = {},
    onOpenRecommendations: () -> Unit = {}
) {
    val context =
        LocalContext.current

    val coroutineScope =
        rememberCoroutineScope()

    val homeDataListener =
        remember {
            WearHomeDataListener(
                context.applicationContext
            )
        }

    val heartRateManager =
        remember {
            HeartRateManager(
                context.applicationContext
            )
        }

    val homeState by
    homeDataListener.state.collectAsState()

    val heartRate by
    heartRateManager.heartRate.collectAsState()

    val heartRatePermission =
        if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                heartRateManager.start()
            }
        }

    LaunchedEffect(Unit) {

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                heartRatePermission
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {

            heartRateManager.start()

        } else {

            permissionLauncher.launch(
                heartRatePermission
            )
        }
    }

    DisposableEffect(homeDataListener) {

        homeDataListener.start()

        onDispose {

            homeDataListener.stop()

            coroutineScope.launch {
                heartRateManager.stop()
            }
        }
    }

    ScreenScaffold(
        timeText = {
            TimeText()
        }
    ) { contentPadding ->

        TransformingLazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                contentPadding,
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            item {

                Text(
                    text = "Resumen de hoy",
                    style =
                        MaterialTheme.typography
                            .titleMedium
                )
            }

            item {

                Card(
                    onClick = {}
                ) {

                    Text(
                        text = "Bienestar",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            homeState.wellbeingScore
                                ?.let {
                                    "${"%.1f".format(it)} / 100"
                                }
                                ?: "-- / 100",
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )

                    Text(
                        text =
                            "Nivel ${
                                translateWearLevel(
                                    homeState.wellbeingLevel
                                )
                            }",
                        style =
                            MaterialTheme.typography
                                .bodyMedium
                    )
                }
            }

            item {

                Card(
                    onClick = {}
                ) {

                    Text(
                        text = "Riesgo preventivo",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            translateWearLevel(
                                homeState.riskLevel
                            ),
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )
                }
            }

            item {

                Card(
                    onClick = {}
                ) {

                    Text(
                        text = "Frecuencia cardiaca",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            heartRate
                                ?.let {
                                    "${it.toInt()} bpm"
                                }
                                ?: "Midiendo...",
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )
                }
            }

            item {

                Button(
                    onClick = onOpenChat
                ) {
                    Text(
                        text = "Asistente"
                    )
                }
            }

            item {

                Button(
                    onClick =
                        onOpenRecommendations
                ) {
                    Text(
                        text = "Recomendaciones"
                    )
                }
            }
        }
    }
}