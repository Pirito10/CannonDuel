package com.psi.cannonduel

import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.PyObject

// Función para gestionar la decisiones de la IA aleatoria
fun handleRandomAI(
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

// Función para gestionar la decisiones de la IA normal
fun handleNormalAI(
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
        val targetCellList = pythonModule.callAttr(
            "choose_shot",
            pythonModule["q_table_shoot"], // Tabla Q de disparos
            intArrayOf(
                player2State.position.first,
                player2State.position.second
            ), // Posición actual
            windDirection, // Dirección del viento
            windStrength, // Fuerza del viento
            gridState.map { row ->
                row.map { if (it) 1 else 0 }.toIntArray()
            }.toTypedArray() // Lista de casillas disponibles
        ).toJava(ArrayList::class.java) as ArrayList<Int>

        val targetCell = Pair(targetCellList[0], targetCellList[1])

        // Elegimos un tipo de munición (por simplicidad, aleatorio entre los disponibles)
        val selectedAmmoType = availableAmmoTypes.random()

        // Procesar el disparo
        processShot(
            targetCell,
            mutableStateOf(selectedAmmoType),
            windDirection,
            windStrength,
            player2State,
            player1State,
            gridState
        )

        // Actualizar la tabla Q de disparos
        val shotReward = calculateShotReward(targetCell, player1State)
        pythonModule.callAttr(
            "update_shoot_q_table",
            pythonModule["q_table_shoot"], // Tabla Q de disparos
            intArrayOf(
                player2State.position.first,
                player2State.position.second
            ), // Posición actual
            windDirection, // Dirección del viento
            windStrength, // Fuerza del viento
            intArrayOf(
                targetCell.first,
                targetCell.second
            ), // Posición objetivo
            shotReward // Recompensa obtenida
        )
    }

    // Comprobar si terminó la partida
    if (checkGameOver(player1State, player2State)) {
        onGameOver()
        return
    }

    // Movimiento basado en la tabla Q
    val availableCellsForMove = getAvailableCells(gridState)

    // Filtrar casillas válidas basadas en la distancia
    var validCells = availableCellsForMove.filter { cell ->
        val distance = calculatePathDistance(player2State.position, cell, gridState)
        distance != null && distance <= player2State.fuel
    }.map {
        intArrayOf(it.first, it.second)
    }.toTypedArray()

    // Comprobar si validCells está vacío
    if (validCells.isEmpty()) {
        validCells = arrayOf(
            intArrayOf(player2State.position.first, player2State.position.second)
        )
    }

    // Llamar al script Python para elegir el movimiento
    val chosenMoveList = pythonModule.callAttr(
        "choose_move",
        pythonModule["q_table_move"], // Tabla Q de movimientos
        intArrayOf(
            player2State.position.first,
            player2State.position.second
        ), // Posición actual
        player2State.fuel, // Combustible restante
        validCells // Casillas válidas
    ).toJava(List::class.java) as ArrayList<Int>

    // Convertir a Pair para Kotlin
    val chosenMove = Pair(chosenMoveList[0], chosenMoveList[1])

    // Procesar el movimiento
    processMove(chosenMove, player2State, player1State, gridState)

    // Actualizar la tabla Q de movimientos
    val moveReward = calculateMoveReward(player2State, player1State)
    pythonModule.callAttr(
        "update_move_q_table",
        pythonModule["q_table_move"], // Tabla Q de disparos
        intArrayOf(
            player2State.position.first,
            player2State.position.second
        ), // Posición actual
        player2State.fuel, // Dirección del viento
        intArrayOf(
            chosenMove.first,
            chosenMove.second
        ), // Posición objetivo
        moveReward // Recompensa obtenida
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