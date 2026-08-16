package com.vitalmind.mobilewear.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.AuthRepository
import com.vitalmind.mobilewear.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AuthRepository(
            ApiClient.authApi
        )

    private val sessionManager =
        SessionManager(application)

    private val _uiState =
        MutableStateFlow(
            LoginUiState()
        )

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _uiState.value =
                LoginUiState(
                    isLoading = true
                )

            try {

                val response =
                    repository.login(
                        email = email,
                        password = password
                    )

                val data = response.data

                if (
                    response.success &&
                    data != null
                ) {

                    sessionManager.saveSession(
                        accessToken =
                            data.accessToken,
                        refreshToken =
                            data.refreshToken,
                        userName =
                            data.user.full_name
                                ?: "Usuario",
                        userEmail =
                            data.user.email
                    )

                    _uiState.value =
                        LoginUiState(
                            loginSuccess = true
                        )

                } else {

                    _uiState.value =
                        LoginUiState(
                            error =
                                response.message
                        )
                }

            } catch (error: Exception) {

                _uiState.value =
                    LoginUiState(
                        error =
                            "No fue posible iniciar sesión. " +
                                    "Verifica tus datos y la conexión."
                    )
            }
        }
    }
}