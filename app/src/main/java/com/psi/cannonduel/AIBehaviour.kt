package com.psi.cannonduel

import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.PyObject

// Función para gestionar la decisiones de la IA aleatoria
fun handleRandomAI(
    playerState: PlayerState,
    enemyState: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onGameOver: () -> Unit
) {
    // Filtramos los tipos de munición disponibles
    val availableAmmoTypes = playerState.ammo.filter { it.value > 0 }.keys

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
            mutableStateOf(selectedAmmoType),
            windDirection,
            windStrength,
            playerState,
            enemyState,
            gridState
        )
    }

    // Comprobamos si se terminó la partida
    if (checkGameOver(playerState, enemyState)) {
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
        if (processMove(cell, playerState, enemyState, gridState)) {
            break
        }
    }
}

// Función para gestionar la decisiones de la IA normal
fun handleNormalAI(
    playerState: PlayerState,
    enemyState: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    knownWindDirection: String,
    knownWindStrength: Int,
    onGameOver: () -> Unit,
    pythonModule: PyObject
) {
    // Obtener los tipos de munición disponibles
    val availableAmmoTypes = playerState.ammo.filter { it.value > 0 }.keys

    // Si hay munición, usamos Q-Learning para disparar
    if (availableAmmoTypes.isNotEmpty()) {
        // Obtenemos la casilla y el tipo de munición
        val result = pythonModule.callAttr(
            "choose_shot",
            pythonModule["q_table_shoot"],
            intArrayOf(playerState.position.first, playerState.position.second),
            intArrayOf(
                enemyState.lastKnownPosition?.first ?: 0,
                enemyState.lastKnownPosition?.second ?: 0
            ),
            knownWindDirection,
            knownWindStrength,
            intArrayOf(
                playerState.ammo["Standard"] ?: 0,
                playerState.ammo["Precision"] ?: 0,
                playerState.ammo["Nuke"] ?: 0
            )
        ).toJava(ArrayList::class.java) as ArrayList<Int>

        val targetCell = Pair(result[0], result[1])
        val ammoType = when (result[2].toInt()) {
            0 -> "Standard"
            1 -> "Precision"
            2 -> "Nuke"
            else -> error("Invalid ammo type")
        }

        // Actualizamos la tabla Q de disparos
        val shotReward = calculateShotReward(targetCell, enemyState)
        pythonModule.callAttr(
            "update_shoot_q_table",
            pythonModule["q_table_shoot"],
            intArrayOf(playerState.position.first, playerState.position.second),
            intArrayOf(
                enemyState.lastKnownPosition?.first ?: 0,
                enemyState.lastKnownPosition?.second ?: 0
            ),
            knownWindDirection,
            knownWindStrength,
            intArrayOf(
                playerState.ammo["Standard"] ?: 0,
                playerState.ammo["Precision"] ?: 0,
                playerState.ammo["Nuke"] ?: 0
            ),
            intArrayOf(targetCell.first, targetCell.second),
            result[2],
            shotReward
        )

        // Procesamos el disparo
        processShot(
            targetCell,
            mutableStateOf(ammoType),
            windDirection,
            windStrength,
            playerState,
            enemyState,
            gridState
        )
    }

    // Comprobamos si terminó la partida
    if (checkGameOver(playerState, enemyState)) {
        onGameOver()
        return
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

    // Filtrar casillas válidas basadas en la distancia
    val validCells = availableCells.filter { cell ->
        val distance = calculatePathDistance(playerState.position, cell, gridState)
        distance != null && distance <= playerState.fuel
    }.map { intArrayOf(it.first, it.second) }.toTypedArray()
        .ifEmpty { arrayOf(intArrayOf(playerState.position.first, playerState.position.second)) }

    // Obtenemos la casilla a la que moverse
    val chosenMoveList = pythonModule.callAttr(
        "choose_move",
        pythonModule["q_table_move"], // Tabla Q de movimientos
        intArrayOf(
            playerState.position.first,
            playerState.position.second
        ), // Posición actual
        playerState.fuel, // Combustible restante
        validCells // Casillas válidas
    ).toJava(List::class.java) as ArrayList<Int>

    val chosenMove = Pair(chosenMoveList[0], chosenMoveList[1])

    // Procesamos el movimiento
    processMove(chosenMove, playerState, enemyState, gridState)

    // Actualizamos la tabla Q de movimientos
    val moveReward = calculateMoveReward(playerState, enemyState)
    pythonModule.callAttr(
        "update_move_q_table",
        pythonModule["q_table_move"],
        intArrayOf(
            playerState.position.first,
            playerState.position.second
        ),
        playerState.fuel,
        intArrayOf(
            chosenMove.first,
            chosenMove.second
        ),
        moveReward
    )
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