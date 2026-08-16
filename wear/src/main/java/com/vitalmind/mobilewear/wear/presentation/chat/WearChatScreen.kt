package com.vitalmind.mobilewear.wear.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.vitalmind.mobilewear.wear.data.WearAnswerListener
import com.vitalmind.mobilewear.wear.data.WearChatClient
import kotlinx.coroutines.launch

private val predefinedQuestions = listOf(
    "¿Cómo está mi bienestar?",
    "¿Qué puedo mejorar hoy?",
    "¿Cómo está mi nivel de riesgo preventivo?",
    "¿Qué debo vigilar?",
    "¿Qué hábito debo priorizar?",
    "¿Cómo puedo dormir mejor?",
    "¿Cómo mejorar mi hidratación?",
    "¿Cómo puedo manejar mi estrés?"
)

@Composable
fun WearChatScreen(
    onBack: () -> Unit
) {
    val context =
        LocalContext.current

    val chatClient =
        remember {
            WearChatClient(
                context.applicationContext
            )
        }

    val answerListener =
        remember {
            WearAnswerListener(
                context.applicationContext
            )
        }

    val scope =
        rememberCoroutineScope()

    val answer by
    answerListener.answer.collectAsState()

    var selectedQuestion by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(
        answerListener
    ) {
        answerListener.start()

        onDispose {
            answerListener.stop()
        }
    }

    LaunchedEffect(answer) {
        if (answer != null) {
            isLoading = false
            errorMessage = null
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
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            item {
                Text(
                    text = "Asistente",
                    style =
                        MaterialTheme.typography
                            .titleMedium
                )
            }

            if (selectedQuestion == null) {

                item {
                    Text(
                        text =
                            "Selecciona una pregunta",
                        style =
                            MaterialTheme.typography
                                .bodyMedium
                    )
                }

                items(
                    count =
                        predefinedQuestions.size
                ) { index ->

                    val question =
                        predefinedQuestions[index]

                    Button(
                        onClick = {

                            selectedQuestion =
                                question

                            isLoading = true
                            errorMessage = null

                            scope.launch {

                                try {

                                    chatClient
                                        .sendQuestion(
                                            question
                                        )

                                } catch (
                                    error: Exception
                                ) {

                                    isLoading = false

                                    errorMessage =
                                        error.message
                                            ?: "No fue posible conectar con el teléfono."
                                }
                            }
                        }
                    ) {

                        Text(
                            text = question,
                            style =
                                MaterialTheme.typography
                                    .labelLarge
                        )
                    }
                }

            } else {

                item {
                    Card(
                        onClick = {}
                    ) {

                        Text(
                            text = "Tu pregunta",
                            style =
                                MaterialTheme.typography
                                    .labelMedium
                        )

                        Text(
                            text =
                                selectedQuestion
                                    .orEmpty(),
                            style =
                                MaterialTheme.typography
                                    .titleSmall
                        )
                    }
                }

                if (isLoading) {

                    item {
                        CircularProgressIndicator()
                    }

                    item {
                        Text(
                            text =
                                "Consultando VitalMind...",
                            style =
                                MaterialTheme.typography
                                    .bodyMedium
                        )
                    }

                } else if (
                    errorMessage != null
                ) {

                    item {
                        Card(
                            onClick = {}
                        ) {

                            Text(
                                text = "No pudimos responder",
                                style =
                                    MaterialTheme.typography
                                        .titleSmall
                            )

                            Text(
                                text =
                                    errorMessage
                                        ?: "Ocurrió un error.",
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        }
                    }

                } else if (
                    answer != null
                ) {

                    item {
                        Card(
                            onClick = {}
                        ) {

                            Text(
                                text = "VitalMind",
                                style =
                                    MaterialTheme.typography
                                        .titleSmall
                            )

                            Text(
                                text =
                                    answer.orEmpty(),
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {

                            selectedQuestion =
                                null

                            isLoading = false
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = "Otra pregunta"
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