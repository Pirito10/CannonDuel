package com.psi.cannonduel

import androidx.compose.runtime.MutableState

// Función principal para manejar la lógica tras presionar el botón de acción
fun handleActionButtonClick(
    actionText: String,
    selectedCell: Pair<Int, Int>?,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onClearSelection: () -> Unit,
    onGameOver: () -> Unit
) {
    when (actionText) {
        // Si la acción era disparar...
        "Shoot" -> handleShoot(
            selectedCell,
            player1State,
            player2State,
            gridState,
            windDirection.value,
            windStrength.value,
            onInfoUpdate,
            onActionChange,
            onGameOver
        )

        // Si la acción era moverse...
        "Move" -> handleMove(selectedCell, gridState, player1State, onInfoUpdate, onActionChange)

        // Si la acción era siguiente turno...
        "Next" -> {
            handleNext(
                player1State,
                player2State,
                gridState,
                windDirection.value,
                windStrength.value,
                onActionChange,
                onGameOver
            )
            updateWind(windDirection, windStrength)
        }
    }

    // Limpiamos la casilla seleccionada
    onClearSelection()
}

// Función para gestionar la lógica de disparar
fun handleShoot(
    selectedCell: Pair<Int, Int>?,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onGameOver: () -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        onInfoUpdate("No cell selected")
        return
    }

    // Procesamos el disparo
    processShot(selectedCell, windDirection, windStrength, player1State, player2State, gridState)
    if (checkGameOver(player1State, player2State)) {
        onGameOver()
    }

    // Cambiamos el botón de acción
    onActionChange("Move")
}

// Función para gestionar la lógica de moverse
fun handleMove(
    selectedCell: Pair<Int, Int>?,
    gridState: Array<Array<Boolean>>,
    player1State: PlayerState,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        onInfoUpdate("No cell selected")
        return
    }

    // Procesamos el movimiento
    if (!processMove(selectedCell, player1State, gridState)) {
        return
    }

    // Cambiamos el botón de acción
    onActionChange("Next")
}

// Función para gestionar la lógica de siguiente turno
fun handleNext(
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onActionChange: (String) -> Unit,
    onGameOver: () -> Unit
) {
    // Generamos una casilla aleatoria
    var randomRow = (0..9).random()
    var randomCol = (0..9).random()
    var selectedCell = Pair(randomRow, randomCol)

    // Procesamos el disparo
    processShot(selectedCell, windDirection, windStrength, player1State, player2State, gridState)
    if (checkGameOver(player1State, player2State)) {
        onGameOver()
    }

    // Intentamos mover a la IA
    var moved = false
    val triedCells = mutableSetOf<Pair<Int, Int>>()
    val totalCells = GRID_SIZE * GRID_SIZE
    while (!moved && triedCells.size < totalCells) {
        // Generamos una casilla aleatoria
        randomRow = (0 until GRID_SIZE).random()
        randomCol = (0 until GRID_SIZE).random()
        selectedCell = Pair(randomRow, randomCol)

        // Si la casilla ya fue probada, la ignoramos
        if (triedCells.contains(selectedCell)) {
            continue
        }

        // Marcamos la casilla como probada
        triedCells.add(selectedCell)

        // Procesamos el movimiento
        moved = processMove(selectedCell, player2State, gridState)
    }

    // Cambiamos el botón de acción
    onActionChange("Shoot")
}

// Función para procesar un disparo
fun processShot(
    targetCell: Pair<Int, Int>,
    windDirection: String,
    windStrength: Int,
    shooterState: PlayerState,
    targetState: PlayerState,
    gridState: Array<Array<Boolean>>
) {
    // Calculamos la casilla golpeada
    val hitCell = calculateHitCell(targetCell, windDirection, windStrength)

    when (hitCell) {
        // Si se golpea a sí mismo
        shooterState.position -> {
            // Reducimos la vida
            shooterState.hp = (shooterState.hp - 1)
            return
        }

        // Si golpea al objetivo
        targetState.position -> {
            // Reducimos la vida
            targetState.hp = (targetState.hp - 1)
            return
        }

        // Si falla marcamos la casilla como destruída
        else -> {
            gridState[hitCell.first][hitCell.second] = false
        }
    }
}

// Función para procesar un movimiento
fun processMove(
    selectedCell: Pair<Int, Int>,
    playerState: PlayerState,
    gridState: Array<Array<Boolean>>
): Boolean {
    // Comprobamos si la casilla está disponible o destruída
    if (!gridState[selectedCell.first][selectedCell.second]) {
        return false
    }

    // Comprobamos si hay un camino válido
    if (!isPathAvailable(playerState.position, selectedCell, gridState)) {
        return false
    }

    // Calculamos la distancia a la casilla seleccionada
    val distance = calculateDistance(playerState.position, selectedCell)

    // Comprobamos si hay suficiente combustible
    if (playerState.fuel < distance) {
        return false
    }

    // Actualizamos el estado del jugador
    playerState.position = selectedCell
    playerState.fuel -= distance
    return true
}

// Función para calcular la casilla golpeada en función del viento
fun calculateHitCell(
    selectedCell: Pair<Int, Int>, windDirection: String, windStrength: Int
): Pair<Int, Int> {
    var (row, col) = selectedCell

    when (windDirection) {
        "N" -> row -= windStrength
        "S" -> row += windStrength
        "E" -> col += windStrength
        "W" -> col -= windStrength
        "NE" -> {
            row -= windStrength
            col += windStrength
        }

        "NW" -> {
            row -= windStrength
            col -= windStrength
        }

        "SE" -> {
            row += windStrength
            col += windStrength
        }

        "SW" -> {
            row += windStrength
            col -= windStrength
        }
    }

    // Nos aseguramos de que la casilla no salga del grid
    row = row.coerceIn(0, 9)
    col = col.coerceIn(0, 9)

    return Pair(row, col)
}

// Función para calcular la distancia entre dos casillas
fun calculateDistance(
    start: Pair<Int, Int>,
    end: Pair<Int, Int>
): Int {
    val rowDistance = kotlin.math.abs(end.first - start.first)
    val colDistance = kotlin.math.abs(end.second - start.second)
    return rowDistance + colDistance
}

// Función para actualizar el viento
fun updateWind(
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>
) {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val maxStrength = 4 // Límite máximo de intensidad del viento

    // Actualizar intensidad
    val strengthChange = listOf(-1, 0, 1, 2).random()
    windStrength.value = (windStrength.value + strengthChange).coerceIn(0, maxStrength)

    // Actualizar dirección
    val currentIndex = directions.indexOf(windDirection.value)
    val directionChange = listOf(-2, -1, 0, 1, 2).random() // Rotación aleatoria (-2 a +2)
    val newIndex = (currentIndex + directionChange + directions.size) % directions.size
    windDirection.value = directions[newIndex]
}

// Función para actualizar el texto de la caja de información
fun updateInfoMessage(infoMessage: MutableState<String>, message: String) {
    infoMessage.value = message
}

// Función para comprobar si hay un camino válido entre dos casillas
fun isPathAvailable(
    start: Pair<Int, Int>,
    end: Pair<Int, Int>,
    gridState: Array<Array<Boolean>>
): Boolean {
    val (startRow, startCol) = start
    val (endRow, endCol) = end

    // Verificamos movimiento horizontal
    if (startRow == endRow) {
        val range = if (startCol < endCol) startCol..endCol else endCol..startCol
        for (col in range) {
            if (!gridState[startRow][col]) {
                return false // Casilla destruida en el camino
            }
        }
    }
    // Verificamos movimiento vertical
    else if (startCol == endCol) {
        val range = if (startRow < endRow) startRow..endRow else endRow..startRow
        for (row in range) {
            if (!gridState[row][startCol]) {
                return false // Casilla destruida en el camino
            }
        }
    }
    // Movimiento diagonal no permitido
    else {
        return false
    }

    return true // Camino válido
}


fun checkGameOver(
    player1State: PlayerState,
    player2State: PlayerState
): Boolean {
    // Condición 1: Vida de los jugadores
    if (player1State.hp <= 0 && player2State.hp <= 0) {
        return true
    } else if (player1State.hp <= 0) {
        return true
    } else if (player2State.hp <= 0) {
        return true
    }

    // Condición 2: Munición
    if (player1State.ammo <= 0 && player2State.ammo <= 0) {
        return true
    }

    return false
}