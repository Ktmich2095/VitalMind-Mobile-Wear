package com.vitalmind.mobilewear.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitalmind.mobilewear.ui.chat.ChatScreen
import com.vitalmind.mobilewear.ui.home.HomeRoute
import com.vitalmind.mobilewear.ui.recommendations.RecommendationsScreen
import com.vitalmind.mobilewear.ui.login.LoginScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitalmind.mobilewear.ui.home.HomeViewModel

object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val RECOMMENDATIONS = "recommendations"
    const val LOGIN = "login"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel=
        viewModel()
    val homeUiState by
        homeViewModel.uiState.collectAsState()
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Routes.HOME) {

            HomeRoute(
                onOpenChat = {
                    navController.navigate(
                        Routes.CHAT
                    )
                },
                onOpenRecommendations = {
                    navController.navigate(
                        Routes.RECOMMENDATIONS
                    )
                },
                viewModel = homeViewModel
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RECOMMENDATIONS) {
            RecommendationsScreen(
                recommendations =
                    homeUiState.recommendations,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}