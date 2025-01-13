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
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.psi.cannonduel.ui.theme.CannonDuelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializamos Chaquopy
        Python.start(AndroidPlatform(this))
        val filesDir = applicationContext.filesDir.absolutePath

        enableEdgeToEdge() // Hace que la aplicación ocupe toda la pantalla (superpuesta a la barra de estado y navegación)
        setContent {
            CannonDuelTheme {
                ManageNavigation(filesDir)
            }
        }
    }
}

// Función para gestionar la navegación entre pantallas
@Composable
fun ManageNavigation(filesDir: String) {
    // Variable con la pantalla actual
    var currentScreen by remember { mutableStateOf("playerSelectionScreen") }
    // Variable con el tipo de jugador seleccionado
    var player by remember { mutableStateOf("User") }
    // Variable con la dificultad seleccionada
    var difficulty by remember { mutableStateOf("Medium") }

    when (currentScreen) {
        "playerSelectionScreen" -> PlayerSelectionScreen(onNextClick = { selectedPlayer ->
            player = selectedPlayer
            currentScreen = "difficultySelectionScreen"
        })

        "difficultySelectionScreen" -> DifficultySelectionScreen(onNextClick = { selectedDifficulty ->
            difficulty = selectedDifficulty
            currentScreen = "gameScreen"
        })

        "gameScreen" -> {
            val python = Python.getInstance()
            val pythonModule: PyObject = python.getModule("hard")

            // Pasar el directorio base al script Python
            pythonModule.callAttr("set_base_dir", filesDir)

            pythonModule.callAttr("initialize_q_tables")
            GameScreen(player = player, difficulty = difficulty, onGameOver = {
                currentScreen = "gameOverScreen"
            }, pythonModule = pythonModule)
        }

        "gameOverScreen" -> GameOverScreen()
    }
}