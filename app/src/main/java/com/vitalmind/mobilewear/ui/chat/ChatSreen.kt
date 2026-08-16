package com.vitalmind.mobilewear.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var question by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = "Asistente VitalMind",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Pregunta lo que quieras sobre tu bienestar y tus resultados."
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = {
                question = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Escribe tu pregunta")
            },
            placeholder = {
                Text(
                    "Ej. ¿Qué puedo hacer para mejorar mi bienestar?"
                )
            },
            minLines = 3,
            maxLines = 6,
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {
                val cleanQuestion =
                    question.trim()

                if (cleanQuestion.isNotEmpty()) {
                    viewModel.askQuestion(
                        cleanQuestion
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled =
                question.isNotBlank() &&
                        !uiState.isLoading
        ) {
            Text(
                text = "Enviar pregunta"
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (uiState.isLoading) {

            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "VitalMind está analizando tu información..."
            )
        }

        uiState.answer?.let { answer ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Respuesta de VitalMind",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    question = ""
                    viewModel.clearAnswer()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nueva pregunta"
                )
            }
        }

        uiState.error?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    viewModel.clearAnswer()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Intentar nuevamente"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volver"
            )
        }
    }
}