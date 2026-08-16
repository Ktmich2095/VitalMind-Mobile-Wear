package com.vitalmind.mobilewear.ui.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitalmind.mobilewear.data.health.SleepManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.HabitRepository

data class SleepUiState(
    val isLoading: Boolean = false,
    val sleepHours: Double? = null,
    val hasPermission: Boolean = false,
    val error: String? = null
)

class SleepViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val habitRepository =
        HabitRepository(
            ApiClient.habitApi
        )

    private val sleepManager =
        SleepManager(
            application
        )

    private val _uiState =
        MutableStateFlow(
            SleepUiState()
        )

    val uiState: StateFlow<SleepUiState> =
        _uiState.asStateFlow()

    val sleepPermissions: Set<String>
        get() =
            setOf(
                sleepManager.sleepPermission
            )

    fun checkPermissionAndLoad() {

        viewModelScope.launch {

            try {

                val hasPermission =
                    sleepManager.hasPermission()

                _uiState.value =
                    _uiState.value.copy(
                        hasPermission = hasPermission,
                        error = null
                    )

                if (hasPermission) {
                    loadSleep()
                }

            } catch (error: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        hasPermission = false,
                        error =
                            "Error verificando Health Connect: ${error.message}"
                    )
            }
        }
    }

    fun loadSleep() {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    hasPermission = true
                )

            try {

                val hours =
                    sleepManager
                        .getLatestSleepHours()

                if (hours != null) {

                    habitRepository
                        .saveSleep(
                            hours
                        )
                }

                _uiState.value =
                    SleepUiState(
                        isLoading = false,
                        sleepHours = hours,
                        hasPermission = true
                    )

            } catch (error: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        error =
                            "No fue posible cargar o sincronizar el sueño: ${error.message}"
                    )
            }
        }
    }
}