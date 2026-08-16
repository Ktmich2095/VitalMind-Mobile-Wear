package com.vitalmind.mobilewear.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.MlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val isLoading: Boolean = false,
    val wellbeingScore: Double? = null,
    val wellbeingLevel: String? = null,
    val riskLevel: String? = null,
    val bmi: Double? = null,
    val recommendations: List<String> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository =
        MlRepository(
            ApiClient.mlApi
        )

    private val _uiState =
        MutableStateFlow(
            HomeUiState()
        )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    fun loadAnalysis() {

        viewModelScope.launch {

            _uiState.value =
                HomeUiState(
                    isLoading = true
                )

            try {

                val response =
                    repository.analyze(
                        analysisDate =
                            LocalDate.now()
                                .toString()
                    )

                val data =
                    response.data

                if (
                    response.success &&
                    data != null
                ) {

                    _uiState.value =
                        HomeUiState(
                            wellbeingScore =
                                data.results
                                    .wellbeing
                                    .score,
                            wellbeingLevel =
                                data.results
                                    .wellbeing
                                    .level,
                            riskLevel =
                                data.results
                                    .risk_classification
                                    .risk_level,
                            bmi =
                                data.results
                                    .calculated_bmi,
                            recommendations =
                                data.results
                                    .recommendations
                        )

                } else {

                    _uiState.value =
                        HomeUiState(
                            error =
                                "No fue posible obtener el análisis."
                        )
                }

            } catch (error: Exception) {

                val message =
                    error.message.orEmpty()

                val userMessage =
                    when {
                        message.contains(
                            "400",
                            ignoreCase = true
                        ) ||
                                message.contains(
                                    "422",
                                    ignoreCase = true
                                ) -> {
                            "Aún no hay suficientes registros " +
                                    "para generar tu análisis."
                        }

                        else -> {
                            "No fue posible conectar con VitalMind."
                        }
                    }

                _uiState.value =
                    HomeUiState(
                        error = userMessage
                    )
            }
        }
    }
}