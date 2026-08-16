package com.vitalmind.mobilewear.wear.presentation.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.vitalmind.mobilewear.wear.data.WearHomeDataListener

@Composable
fun WearRecommendationsScreen(
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val dataListener =
        remember {
            WearHomeDataListener(
                context.applicationContext
            )
        }

    val state by
    dataListener.state.collectAsState()

    DisposableEffect(
        dataListener
    ) {

        dataListener.start()

        onDispose {
            dataListener.stop()
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
                Arrangement.spacedBy(8.dp)
        ) {

            item {

                Text(
                    text = "Recomendaciones",
                    style =
                        MaterialTheme.typography
                            .titleLarge
                )
            }

            if (
                state.recommendations.isEmpty()
            ) {

                item {

                    Text(
                        text =
                            "No hay recomendaciones disponibles."
                    )
                }

            } else {

                items(
                    count =
                        state.recommendations.size
                ) { index ->

                    val recommendation =
                        state.recommendations[index]

                    Card(
                        onClick = {}
                    ) {

                        Text(
                            text =
                                recommendation
                        )
                    }
                }
            }

            item {

                Button(
                    onClick = onBack
                ) {

                    Text(
                        text = "Volver"
                    )
                }
            }
        }
    }
}