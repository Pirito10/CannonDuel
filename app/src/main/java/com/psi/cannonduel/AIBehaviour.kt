package com.psi.cannonduel

import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.PyObject

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
    onGameOver: () -> Unit,
    pythonModule: PyObject
) {
    // Obtener los tipos de munición disponibles
    val availableAmmoTypes = player2State.ammo.filter { it.value > 0 }.keys

    // Disparo basado en la tabla Q
    if (availableAmmoTypes.isNotEmpty()) {
        // Generamos las casillas disponibles
        val availableCells = getAvailableCells(gridState)

        // Elegir celda usando Q-table
        val chosenCell = pythonModule.callAttr(
            "handle_turn",
            "shoot",
            windDirection,
            availableCells
        ).toJava(Pair::class.java) as Pair<Int, Int>

        // Elegimos un tipo de munición (por simplicidad, aleatorio entre los disponibles)
        val selectedAmmoType = availableAmmoTypes.random()

        // Procesar el disparo
        processShot(
            chosenCell,
            selectedAmmo = mutableStateOf(selectedAmmoType),
            windDirection,
            windStrength,
            player2State,
            player1State,
            gridState
        )

        // Actualizar la tabla Q de disparo
        pythonModule.callAttr(
            "handle_turn",
            "update_shoot",
            windDirection,
            chosenCell,
            calculateShotReward(chosenCell, player1State) // Recompensa
        )
    }

    // Comprobar si terminó la partida
    if (checkGameOver(player1State, player2State)) {
        onGameOver()
        return
    }

    // Movimiento basado en la tabla Q
    val availableCellsForMove = getAvailableCells(gridState)
    val chosenMove = pythonModule.callAttr(
        "handle_turn",
        "move",
        availableCellsForMove,
        player2State.fuel,
        player2State.position
    ).toJava(Pair::class.java) as Pair<Int, Int>

    // Procesar el movimiento
    if (processMove(chosenMove, player2State, player1State, gridState)) {
        // Actualizar la tabla Q de movimiento
        pythonModule.callAttr(
            "handle_turn",
            "update_move",
            player2State.position,
            chosenMove,
            calculateMoveReward(player2State, player1State) // Recompensa
        )
    }
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

fun calculateShotReward(target: Pair<*, *>, enemyState: PlayerState): Int {
    return when (target) {
        enemyState.position -> 20 // Impacto directo
        else -> -2 // Penalización por fallo
    }
}

fun calculateMoveReward(playerState: PlayerState, enemyState: PlayerState): Int {
    return if (playerState.position == enemyState.position) {
        -10 // Penalización por moverse al mismo lugar que el enemigo
    } else {
        5 // Recompensa por moverse estratégicamente
    }
}
