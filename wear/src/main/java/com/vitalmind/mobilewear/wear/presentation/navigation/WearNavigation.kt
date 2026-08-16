package com.vitalmind.mobilewear.wear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.vitalmind.mobilewear.wear.presentation.WearHomeScreen
import com.vitalmind.mobilewear.wear.presentation.chat.WearChatScreen
import com.vitalmind.mobilewear.wear.presentation.recommendations.WearRecommendationsScreen

object WearRoutes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val RECOMMENDATIONS = "recommendations"
}

@Composable
fun WearNavigation() {

    val navController =
        rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = WearRoutes.HOME
    ) {

        composable(WearRoutes.HOME) {

            WearHomeScreen(
                onOpenChat = {
                    navController.navigate(
                        WearRoutes.CHAT
                    )
                },
                onOpenRecommendations = {
                    navController.navigate(
                        WearRoutes.RECOMMENDATIONS
                    )
                }
            )
        }

        composable(WearRoutes.CHAT) {

            WearChatScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            WearRoutes.RECOMMENDATIONS
        ) {

            WearRecommendationsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}