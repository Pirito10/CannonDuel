package com.psi.cannonduel

// Función para gestionar la decisiones de la IA nivel fácil
fun handleEasyAI(
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onGameOver: () -> Unit
) {
    // Generamos una casilla aleatoria
    val randomRow = (0 until GRID_SIZE).random()
    val randomCol = (0 until GRID_SIZE).random()
    val selectedCell = Pair(randomRow, randomCol)

    // Procesamos el disparo
    processShot(
        selectedCell,
        windDirection,
        windStrength,
        player1State,
        player2State,
        gridState
    )

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
        if (processMove(cell, player2State, gridState)) {
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