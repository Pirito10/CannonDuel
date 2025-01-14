package com.psi.cannonduel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.PyObject

// Función para gestionar la decisiones de la IA aleatoria
fun handleRandomAI(
    playerState: PlayerState,
    enemyState: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    infoMessage: MutableState<String>,
    onGameOver: (PlayerState, PlayerState) -> Unit
) {
    // Filtramos los tipos de munición disponibles
    val availableAmmoTypes = playerState.ammo.filter { it.value > 0 }.keys

    // Disparamos si nos queda munición
    if (availableAmmoTypes.isNotEmpty()) {
        // Generamos una casilla aleatoria
        val randomRow = (0 until GRID_SIZE - 1).random()
        val randomCol = (0 until GRID_SIZE - 1).random()
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
            gridState,
            infoMessage
        )
    }

    // Comprobamos si se terminó la partida
    if (checkGameOver(playerState, enemyState)) {
        onGameOver(playerState, enemyState)
    }

    // Generamos una lista de casillas disponibles en el grid
    val availableCells = mutableListOf<Pair<Int, Int>>()
    for (row in 0 until GRID_SIZE - 1) {
        for (col in 0 until GRID_SIZE - 1) {
            if (gridState[row][col]) {
                availableCells.add(Pair(row, col))
            }
        }
    }
    // Barajamos la lista
    val shuffledAvailableCells = availableCells.shuffled()

    // Recorremos la lista hasta encontrar una casilla válida para moverse a ella
    for (cell in shuffledAvailableCells) {
        if (processMove(cell, playerState, enemyState, gridState, infoMessage)) {
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
    infoMessage: MutableState<String>,
    onGameOver: (PlayerState, PlayerState) -> Unit,
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

        val targetCell = Pair(result[0].toInt(), result[1].toInt())
        val ammoType = when (result[2].toInt()) {
            0 -> "Standard"
            1 -> "Precision"
            2 -> "Nuke"
            else -> error("Invalid ammo type")
        }

        // Procesamos el disparo
        val hit = processShot(
            targetCell,
            mutableStateOf(ammoType),
            windDirection,
            windStrength,
            playerState,
            enemyState,
            gridState,
            infoMessage
        )

        // Actualizamos la tabla Q de disparos
        val shotReward = calculateShotReward(hit)
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
    }

    // Comprobamos si terminó la partida
    if (checkGameOver(playerState, enemyState)) {
        onGameOver(playerState, enemyState)
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
        pythonModule["q_table_move"],
        intArrayOf(
            playerState.position.first,
            playerState.position.second
        ),
        playerState.fuel,
        validCells
    ).toJava(List::class.java) as ArrayList<Int>

    val chosenMove = Pair(chosenMoveList[0].toInt(), chosenMoveList[1].toInt())

    // Procesamos el movimiento
    processMove(chosenMove, playerState, enemyState, gridState, infoMessage)

    // Actualizamos la tabla Q de movimientos
    val moveReward = calculateMoveReward(playerState)
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

// Función para calcular la recompensa tras disparar
fun calculateShotReward(hit: Boolean): Int {
    return when (hit) {
        true -> 20
        false -> -2
    }
}

// Función para calcular la recompensa tras moverse
fun calculateMoveReward(playerState: PlayerState): Int {
    return if (playerState.hp == playerState.previousHp) {
        5
    } else {
        -10
    }
}