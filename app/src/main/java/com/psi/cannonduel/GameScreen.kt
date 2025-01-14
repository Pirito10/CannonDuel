package com.psi.cannonduel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaquo.python.PyObject
import kotlin.random.Random

// Constantes con valores por defecto
const val MAX_HP = 5
const val MAX_STANDARD_AMMO = 6
const val MAX_PRECISION_AMMO = 3
const val MAX_NUKE_AMMO = 1
const val MAX_FUEL = 20
const val GRID_SIZE = 6

// Clase para encapsular el estado de los jugadores
data class PlayerState(
    var hp: Int,
    var previousHp: Int,
    var ammo: MutableMap<String, Int>,
    var fuel: Int,
    var position: Pair<Int, Int>,
    var lastKnownPosition: Pair<Int, Int>? = null
)

// Función para crear el estado de un jugador
fun createPlayerState(row: Int): PlayerState {
    // Generamos la casilla inicial aleatoriamente
    val startingCell = Pair(row, Random.nextInt(GRID_SIZE))

    return PlayerState(
        MAX_HP,
        MAX_HP,
        mutableMapOf(
            "Standard" to MAX_STANDARD_AMMO,
            "Precision" to MAX_PRECISION_AMMO,
            "Nuke" to MAX_NUKE_AMMO
        ),
        MAX_FUEL,
        startingCell
    )
}

// Función para la pantalla de juego
@Composable
fun GameScreen(
    gamemode: String,
    difficulty: String,
    pythonModule: PyObject,
    onGameOver: (PlayerState, PlayerState) -> Unit
) {
    // Estados de los jugadores
    val player1State = remember { mutableStateOf(createPlayerState(5)) }
    val player2State = remember { mutableStateOf(createPlayerState(0)) }
    // Estado del viento
    val windDirection = remember { mutableStateOf("N") }
    val windStrength = remember { mutableIntStateOf(0) }
    val knownWindDirection = remember { mutableStateOf("?") }
    val knownWindStrength = remember { mutableIntStateOf(0) }
    // Selectores
    val selectedCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val selectedAmmo = remember { mutableStateOf("Standard") }
    // Estado del grid (true -> casilla disponible, false -> casilla destruída)
    val gridState =
        remember { mutableStateOf(Array(GRID_SIZE) { Array(GRID_SIZE) { true } }) } // True -> casilla disponible, false -> casilla destruída
    // Textos
    val actionButtonText = remember { mutableStateOf("Shoot") }
    val infoMessage = remember { mutableStateOf("Choose a target") }

    // Si el modo de juego es IA vs IA, ejecutamos directamente la partida sin mostrar la interfaz
    if (gamemode == "AI vs AI") {
        handleActionButtonClick(
            gamemode,
            difficulty,
            actionButtonText.value,
            selectedCell.value,
            selectedAmmo,
            player1State.value,
            player2State.value,
            gridState.value,
            windDirection,
            windStrength,
            knownWindDirection,
            knownWindStrength,
            infoMessage,
            pythonModule,
            { actionButtonText.value = it },
            { selectedCell.value = null },
            onGameOver
        )
    }

    // Contenedor que ocupa toda la pantalla
    Box(Modifier.fillMaxSize()) {
        // Barra inferior para el jugador 1
        PlayerBar(
            "Player 1 (You)",
            player1State.value.hp / MAX_HP.toFloat(),
            Modifier.align(Alignment.BottomCenter)
        )

        // Barra superior para el jugador 2
        PlayerBar(
            "Player 2 (AI)",
            player2State.value.hp / MAX_HP.toFloat(),
            Modifier.align(Alignment.TopCenter)
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp)
                .align(Alignment.TopCenter),
            thickness = 1.dp
        )

        // Contenedor central para grid, caja de información y barra de combustible
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Información del viento
            WindInfo(knownWindDirection.value, knownWindStrength.intValue)

            // Grid de juego
            GameGrid(
                GRID_SIZE,
                gridState.value,
                player1State.value.position,
                player2State.value.lastKnownPosition ?: Pair(-1, -1),
                selectedCell.value
            ) { selectedCell.value = it }

            Spacer(Modifier.height(24.dp))

            // Caja de información
            InfoBox(infoMessage.value)

            Spacer(Modifier.height(12.dp))

            // Barra de combustible
            FuelBar(player1State.value.fuel / MAX_FUEL.toFloat())
        }

        // Contenedor inferior para selector de munición y botón de acción
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selector de munición
            AmmoSelector(
                selectedAmmo.value,
                player1State.value.ammo
            ) { selectedAmmo.value = it }

            // Botón de acción
            ActionButton(actionButtonText.value) {
                handleActionButtonClick(
                    gamemode,
                    difficulty,
                    actionButtonText.value,
                    selectedCell.value,
                    selectedAmmo,
                    player1State.value,
                    player2State.value,
                    gridState.value,
                    windDirection,
                    windStrength,
                    knownWindDirection,
                    knownWindStrength,
                    infoMessage,
                    pythonModule,
                    { actionButtonText.value = it },
                    { selectedCell.value = null },
                    onGameOver
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 52.dp)
                .align(Alignment.BottomCenter),
            thickness = 1.dp
        )
    }
}