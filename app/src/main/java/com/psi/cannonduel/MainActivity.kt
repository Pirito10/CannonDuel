package com.psi.cannonduel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.psi.cannonduel.ui.theme.CannonDuelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Hace que la aplicación ocupe toda la pantalla (superpuesta a la barra de estado y navegación)
        setContent {
            CannonDuelTheme {
                AppContent()
            }
        }
    }
}

// Función para gestionar la navegación entre pantallas
@Composable
fun AppContent() {
    // Variable con la pantalla actual
    var currentScreen by remember { mutableStateOf("playerSelectionScreen") }

    when (currentScreen) {
        "playerSelectionScreen" -> PlayerSelectionScreen(onNextClick = {
            currentScreen = "difficultySelectionScreen"
        })

        "difficultySelectionScreen" -> DifficultySelectionScreen(onNextClick = {
            currentScreen = "gameScreen"
        })

        "gameScreen" -> GameScreen()
    }
}