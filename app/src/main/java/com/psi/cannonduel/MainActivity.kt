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

        // Inicializamos Python y obtenemos el script con el modelo para la IA
        Python.start(AndroidPlatform(this))
        val python = Python.getInstance()
        val pythonModule: PyObject = python.getModule("AI")
        // Obtenemos el directorio de ficheros de la aplicación y inicializamos el script
        val filesDir = applicationContext.filesDir.absolutePath
        pythonModule.callAttr("set_base_dir", filesDir)
        pythonModule.callAttr("initialize_q_tables")

        // Hacemos que la aplicación ocupe toda la pantalla (superpuesta a la barra de estado y navegación)
        enableEdgeToEdge()
        setContent {
            CannonDuelTheme {
                // Mostramos la pantalla de inicio
                ManageNavigation(pythonModule)
            }
        }
    }
}

// Función para gestionar la navegación entre pantallas
@Composable
fun ManageNavigation(pythonModule: PyObject) {
    // Estado de la navegación
    var currentScreen by remember { mutableStateOf("gamemodeSelectionScreen") }
    var gamemode by remember { mutableStateOf("User vs AI") }
    var difficulty by remember { mutableStateOf("Normal") }
    var player1State: PlayerState? = null
    var player2State: PlayerState? = null

    // Mostramos la pantalla correspondiente
    when (currentScreen) {
        "gamemodeSelectionScreen" -> GamemodeSelectionScreen { selectedGamemode ->
            gamemode = selectedGamemode
            currentScreen = "difficultySelectionScreen"
        }

        "difficultySelectionScreen" -> DifficultySelectionScreen { selectedDifficulty ->
            difficulty = selectedDifficulty
            currentScreen = if (gamemode == "User vs AI" || gamemode == "AI vs AI") {
                "gameScreen"
            } else {
                "trainingScreen"
            }
        }

        "gameScreen" -> GameScreen(gamemode, difficulty, pythonModule) { player1, player2 ->
            player1State = player1
            player2State = player2
            currentScreen = "gameOverScreen"
        }

        // TODO implementar más tarde
        //"trainingScreen" -> TrainingScreen()

        "gameOverScreen" -> GameOverScreen(player1State, player2State)
    }
}