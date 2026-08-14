package com.vitalmind.mobilewear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vitalmind.mobilewear.navigation.AppNavigation
import com.vitalmind.mobilewear.ui.theme.VitalMindMobileWearTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            VitalMindMobileWearTheme {
                AppNavigation()
            }
        }
    }
}