package com.vitalmind.mobilewear.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitalmind.mobilewear.ui.chat.ChatScreen
import com.vitalmind.mobilewear.ui.home.HomeScreen
import com.vitalmind.mobilewear.ui.recommendations.RecommendationsScreen
import com.vitalmind.mobilewear.ui.login.LoginScreen

object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val RECOMMENDATIONS = "recommendations"
    const val LOGIN = "login"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
            HomeScreen(
                onOpenChat = {
                    navController.navigate(Routes.CHAT)
                },
                onOpenRecommendations = {
                    navController.navigate(
                        Routes.RECOMMENDATIONS
                    )
                }
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
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}