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
import androidx.compose.runtime.rememberUpdatedState
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
import com.vitalmind.mobilewear.wear.data.WearSensorClient
import com.vitalmind.mobilewear.wear.health.HeartRateManager
import com.vitalmind.mobilewear.wear.health.StepsManager
import com.vitalmind.mobilewear.wear.presentation.navigation.WearNavigation
import com.vitalmind.mobilewear.wear.presentation.theme.VitalMindWearTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vitalmind.mobilewear.wear.data.WearConnectionManager
import kotlin.time.Duration.Companion.milliseconds

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

    /*
     * Datos enviados desde el teléfono
     */
    val homeDataListener =
        remember {
            WearHomeDataListener(
                context.applicationContext
            )
        }

    val homeState by
    homeDataListener.state.collectAsState()

    /*
     * Frecuencia cardiaca
     */
    val heartRateManager =
        remember {
            HeartRateManager(
                context.applicationContext
            )
        }

    val heartRate by
    heartRateManager
        .heartRate
        .collectAsState()

    val currentHeartRateState =
        rememberUpdatedState(
            heartRate
        )

    /*
     * Pasos + distancia
     */
    val stepsManager =
        remember {
            StepsManager(
                context.applicationContext
            )
        }

    val steps by
    stepsManager
        .steps
        .collectAsState()

    val distanceMeters by
    stepsManager
        .distanceMeters
        .collectAsState()

    val currentDistanceState =
        rememberUpdatedState(
            distanceMeters
        )

    val currentStepsState =
        rememberUpdatedState(
            steps
        )
    val calories by
        stepsManager
            .calories
            .collectAsState()

    val currentCaloriesState =
        rememberUpdatedState(
            calories
        )
    /*
     * Comunicación Wear → móvil
     */
    val sensorClient =
        remember {
            WearSensorClient(
                context.applicationContext
            )
        }

    /*
     * Permiso frecuencia cardiaca
     */
    val heartRatePermission =
        if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }

    val heartRatePermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) { granted ->

            if (granted) {
                heartRateManager.start()
            }
        }

    /*
     * Permiso actividad física
     */
    val activityPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) { granted ->

            if (granted) {

                coroutineScope.launch {
                    stepsManager.start()
                }
            }
        }
    val connectionManager =
        remember {
            WearConnectionManager(
                context.applicationContext
            )
        }

    val isPhoneConnected by
    connectionManager
        .isConnected
        .collectAsState()

    LaunchedEffect(Unit) {

        while (true) {

            connectionManager
                .checkConnection()

            delay(
                15_000L.milliseconds
            )
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            delay(60_000L)

            val currentCalories =
                currentCaloriesState.value

            if (currentCalories != null) {

                try {

                    sensorClient.sendCalories(
                        currentCalories
                    )

                    android.util.Log.d(
                        "VitalMindSensor",
                        "Calorías sincronizadas: $currentCalories kcal"
                    )

                } catch (error: Exception) {

                    android.util.Log.e(
                        "VitalMindSensor",
                        "No fue posible sincronizar las calorías",
                        error
                    )
                }
            }
        }
    }
    /*
     * Iniciar frecuencia cardiaca
     */
    LaunchedEffect(Unit) {

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                heartRatePermission
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {

            heartRateManager.start()

        } else {

            heartRatePermissionLauncher.launch(
                heartRatePermission
            )
        }
    }

    /*
     * Iniciar pasos y distancia
     */
    LaunchedEffect(Unit) {

        val activityPermissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (activityPermissionGranted) {

            stepsManager.start()

        } else {

            activityPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        }
    }

    /*
     * Sincronizar frecuencia con móvil
     * cada 60 segundos
     */
    LaunchedEffect(Unit) {

        while (true) {

            delay(
                60_000L
            )

            val currentHeartRate =
                currentHeartRateState
                    .value
                    ?.toInt()

            if (currentHeartRate != null) {

                try {

                    sensorClient.sendHeartRate(
                        currentHeartRate
                    )

                    android.util.Log.d(
                        "VitalMindSensor",
                        "Frecuencia sincronizada: $currentHeartRate bpm"
                    )

                } catch (error: Exception) {

                    android.util.Log.e(
                        "VitalMindSensor",
                        "No fue posible sincronizar la frecuencia",
                        error
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            delay(
                60_000L
            )

            val currentDistanceMeters =
                currentDistanceState.value

            if (currentDistanceMeters != null) {

                val distanceKm =
                    currentDistanceMeters / 1000.0

                try {

                    sensorClient.sendDistanceKm(
                        distanceKm
                    )

                    android.util.Log.d(
                        "VitalMindSensor",
                        "Distancia sincronizada: $distanceKm km"
                    )

                } catch (error: Exception) {

                    android.util.Log.e(
                        "VitalMindSensor",
                        "No fue posible sincronizar la distancia",
                        error
                    )
                }
            }
        }
    }

    /*
     * Sincronizar pasos con móvil
     * cada 60 segundos
     */
    LaunchedEffect(Unit) {

        while (true) {

            delay(
                60_000L
            )

            val currentSteps =
                currentStepsState.value

            if (currentSteps != null) {

                try {

                    sensorClient.sendSteps(
                        currentSteps
                    )

                    android.util.Log.d(
                        "VitalMindSensor",
                        "Pasos sincronizados: $currentSteps"
                    )

                } catch (error: Exception) {

                    android.util.Log.e(
                        "VitalMindSensor",
                        "No fue posible sincronizar los pasos",
                        error
                    )
                }
            }
        }
    }

    /*
     * Listener móvil → reloj
     */
    DisposableEffect(
        homeDataListener
    ) {

        homeDataListener.start()

        onDispose {

            homeDataListener.stop()

            coroutineScope.launch {

                heartRateManager.stop()
                stepsManager.stop()
            }
        }
    }

    /*
     * Interfaz
     */
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
                Arrangement.spacedBy(
                    10.dp
                )
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
                        text =
                            if (isPhoneConnected) {
                                "Teléfono conectado"
                            } else {
                                "Teléfono desconectado"
                            },
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            if (isPhoneConnected) {
                                "VitalMind está sincronizando tus datos."
                            } else {
                                "Conecta tu teléfono para sincronizar."
                            },
                        style =
                            MaterialTheme.typography
                                .bodyMedium
                    )
                }
            }

            /*
             * Bienestar
             */
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
                            homeState
                                .wellbeingScore
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

            /*
             * Riesgo
             */
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

            /*
             * Frecuencia cardiaca
             */
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

            /*
             * Pasos
             */
            item {

                Card(
                    onClick = {}
                ) {

                    Text(
                        text = "Pasos de hoy",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            steps
                                ?.let {
                                    "$it pasos"
                                }
                                ?: "Calculando...",

                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )
                }
            }

            /*
             * Distancia
             */
            item {

                Card(
                    onClick = {}
                ) {

                    Text(
                        text = "Distancia de hoy",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            distanceMeters
                                ?.let {
                                    "${
                                        "%.2f".format(
                                            it / 1000.0
                                        )
                                    } km"
                                }
                                ?: "Calculando...",

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
                        text = "Calorías de hoy",
                        style =
                            MaterialTheme.typography
                                .labelLarge
                    )

                    Text(
                        text =
                            calories
                                ?.let {
                                    "${it.toInt()} kcal"
                                }
                                ?: "Calculando...",
                        style =
                            MaterialTheme.typography
                                .titleLarge
                    )
                }
            }

            /*
             * Navegación
             */
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