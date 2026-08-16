package com.vitalmind.mobilewear.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ChatUiState(
    val isLoading: Boolean = false,
    val answer: String? = null,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val repository =
        ChatRepository(
            ApiClient.chatApi
        )

    private val _uiState =
        MutableStateFlow(
            ChatUiState()
        )

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    fun askQuestion(
        question: String
    ) {
        viewModelScope.launch {

            _uiState.value =
                ChatUiState(
                    isLoading = true
                )

            try {

                val response =
                    repository.ask(
                        analysisDate =
                            LocalDate.now()
                                .toString(),
                        message = question
                    )

                val data = response.data

                if (
                    response.success &&
                    data != null
                ) {
                    _uiState.value =
                        ChatUiState(
                            answer =
                                data.answer
                        )
                } else {
                    _uiState.value =
                        ChatUiState(
                            error =
                                "No fue posible generar una respuesta."
                        )
                }

            } catch (error: Exception) {

                val message =
                    error.message.orEmpty()

                _uiState.value =
                    ChatUiState(
                        error =
                            if (
                                message.contains("400") ||
                                message.contains("422")
                            ) {
                                "Aún no hay suficientes datos para responder esta pregunta."
                            } else {
                                "No fue posible conectar con VitalMind."
                            }
                    )
            }
        }
    }

    fun clearAnswer() {
        _uiState.value =
            ChatUiState()
    }
}