package com.psi.cannonduel

import androidx.compose.runtime.mutableStateOf

// Función para gestionar la decisiones de la IA nivel fácil
fun handleEasyAI(
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onGameOver: () -> Unit
) {
    // Filtramos los tipos de munición disponibles
    val availableAmmoTypes = player2State.ammo.filter { it.value > 0 }.keys

    // Disparamos si nos queda munición
    if (availableAmmoTypes.isNotEmpty()) {
        // Generamos una casilla aleatoria
        val randomRow = (0 until GRID_SIZE).random()
        val randomCol = (0 until GRID_SIZE).random()
        val selectedCell = Pair(randomRow, randomCol)

        // Elegimos un tipo de munición aleatorio
        val selectedAmmoType = availableAmmoTypes.random()

        // Procesamos el disparo
        processShot(
            selectedCell,
            selectedAmmo = mutableStateOf(selectedAmmoType),
            windDirection,
            windStrength,
            player2State,
            player1State,
            gridState
        )
    }

    // Comprobamos si se terminó la partida
    if (checkGameOver(player1State, player2State)) {
        onGameOver()
    }

    // Generamos una lista de casillas disponibles en el grid
    val availableCells = mutableListOf<Pair<Int, Int>>()
    for (row in 0 until GRID_SIZE) {
        for (col in 0 until GRID_SIZE) {
            if (gridState[row][col]) {
                availableCells.add(Pair(row, col))
            }
        }
    }
    // Barajamos la lista
    val shuffledAvailableCells = availableCells.shuffled()

    // Recorremos la lista hasta encontrar una casilla válida para moverse a ella
    for (cell in shuffledAvailableCells) {
        if (processMove(cell, player2State, player1State, gridState)) {
            break
        }
    }
}

// Función para gestionar la decisiones de la IA nivel medio
fun handleMediumAI(
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onGameOver: () -> Unit
) {
}

// Función para gestionar la decisiones de la IA nivel difícil
fun handleHardAI(
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onGameOver: () -> Unit
) {
}