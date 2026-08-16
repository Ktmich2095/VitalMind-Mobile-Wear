package com.vitalmind.mobilewear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitalmind.mobilewear.ui.theme.VitalMindMobileWearTheme

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            VitalMindMobileWearTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text = "Datos de salud",
                        style =
                            MaterialTheme.typography
                                .headlineMedium
                    )

                    Text(
                        text =
                            "VitalMind utiliza tus datos de sueño para mostrar tu descanso registrado y mejorar tu análisis de bienestar."
                    )
                }
            }
        }
    }
}