package com.vitalmind.mobilewear.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitalmind.mobilewear.ui.theme.VitalMindMobileWearTheme
import androidx.compose.ui.platform.LocalContext
import com.vitalmind.mobilewear.data.wear.WearHomeSyncClient


private fun translateLevel(
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
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenChat: () -> Unit = {},
    onOpenRecommendations: () -> Unit = {},
    uiState: HomeUiState = HomeUiState()
) {


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "VitalMind AI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Tu bienestar, más cerca de ti",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Bienestar de hoy",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = uiState.wellbeingScore
                        ?.let {
                            "${"%.1f".format(it)} / 100"
                        }
                        ?: "-- / 100",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (
                        uiState.wellbeingLevel != null
                    ) {
                        "Nivel ${
                            translateLevel(
                                uiState.wellbeingLevel
                            )
                        }"
                    } else {
                        "Sin datos"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Riesgo preventivo",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = translateLevel(
                        uiState.riskLevel
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onOpenChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Hablar con VitalMind"
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onOpenRecommendations,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ver recomendaciones"
            )
        }
    }
}

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onOpenChat: () -> Unit = {},
    onOpenRecommendations: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val wearSyncClient =
        remember {
            WearHomeSyncClient(
                context.applicationContext
            )

        }
    LaunchedEffect(Unit) {
        viewModel.loadAnalysis()
    }
    LaunchedEffect(
        uiState.wellbeingScore,
        uiState.wellbeingLevel,
        uiState.riskLevel
    ) {

        wearSyncClient.syncHome(
            wellbeingScore =
                uiState.wellbeingScore,
            wellbeingLevel =
                uiState.wellbeingLevel,
            riskLevel =
                uiState.riskLevel
        )
    }

    HomeScreen(
        modifier = modifier,
        onOpenChat = onOpenChat,
        onOpenRecommendations = onOpenRecommendations,
        uiState = uiState
    )
}

@Preview(
    showBackground = true
)
@Composable
fun HomeScreenPreview() {
    VitalMindMobileWearTheme {
        HomeScreen(
            uiState = HomeUiState(
                wellbeingScore = 55.7,
                wellbeingLevel = "medium",
                riskLevel = "medium"
            )
        )
    }
}