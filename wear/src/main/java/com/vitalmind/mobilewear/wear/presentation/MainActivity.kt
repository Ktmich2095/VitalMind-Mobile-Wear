package com.vitalmind.mobilewear.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.vitalmind.mobilewear.wear.presentation.navigation.WearNavigation
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.vitalmind.mobilewear.wear.data.WearHomeDataListener

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
    MaterialTheme {
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
    onOpenChat:()-> Unit={},
    onOpenRecommendations:()->Unit={}
) {
    val context =
        LocalContext.current
    val homeDataListener =
        remember {
            WearHomeDataListener(
                context.applicationContext
            )
        }
    val homeState by
        homeDataListener.state.collectAsState()

    DisposableEffect(homeDataListener) {
        homeDataListener.start()

        onDispose {
            homeDataListener.stop()
        }
    }
    ScreenScaffold(
        timeText = {
            TimeText()
        }
    ) { contentPadding ->

        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            item {
                Text(
                    text = "VitalMind",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text(
                    text = "Resumen de hoy",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Card(
                    onClick = {}
                ) {
                    Text(
                        text = "Bienestar"
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
                                .titleMedium
                    )

                    Text(
                        text =
                            "Nivel ${
                                translateWearLevel(
                                    homeState.wellbeingLevel
                                )
                            }"
                    )
                }
            }

            item {
                Card(
                    onClick = {}
                ) {
                    Text(
                        text = "Riesgo preventivo"
                    )

                    Text(
                        text =
                            translateWearLevel(
                                homeState.riskLevel
                            ),
                        style =
                            MaterialTheme.typography
                                .titleMedium
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
                    onClick = onOpenRecommendations
                ) {
                    Text(
                        text = "Recomendaciones"
                    )
                }
            }
        }
    }
}