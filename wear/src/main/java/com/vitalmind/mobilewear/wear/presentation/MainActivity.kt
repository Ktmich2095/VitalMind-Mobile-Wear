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

@Composable
fun WearHomeScreen(
    onOpenChat:()-> Unit={},
    onOpenRecommendations:()->Unit={}
) {

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
                        text = "71 / 100",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Nivel medio"
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
                        text = "Bajo",
                        style = MaterialTheme.typography.titleMedium
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