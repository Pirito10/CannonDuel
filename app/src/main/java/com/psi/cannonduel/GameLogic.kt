package com.psi.cannonduel

import androidx.compose.runtime.MutableState
import java.util.LinkedList

// Función principal para manejar la lógica tras presionar el botón de acción
fun handleActionButtonClick(
    difficulty: String,
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
        "Move" -> handleMove(
            selectedCell,
            gridState,
            player1State,
            onInfoUpdate,
            onActionChange
        )

        // Si la acción era siguiente turno...
        "Next" -> {
            handleNext(
                difficulty,
                player1State,
                player2State,
                gridState,
                windDirection.value,
                windStrength.value,
                onActionChange,
                onGameOver
            )
            // Actualizamos el viento
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
    difficulty: String,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onActionChange: (String) -> Unit,
    onGameOver: () -> Unit
) {
    when (difficulty) {
        "Easy" -> handleEasyAI(
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            onGameOver
        )

        "Medium" -> handleMediumAI(
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            onGameOver
        )

        "Hard" -> handleHardAI(
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            onGameOver
        )
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
    // TODO comprobar que el otro jugador no esté en la casilla destino
    // Comprobamos que la casilla no esté destruída
    if (!gridState[selectedCell.first][selectedCell.second]) {
        return false
    }

    // Calculamos la distancia a la casilla seleccionada
    val distance = calculatePathDistance(playerState.position, selectedCell, gridState)

    // Comprobamos que haya un camino válido
    if (distance == null) {
        return false
    }

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

// Función para buscar el menor camino entre dos casillas y calcular la distancia
fun calculatePathDistance(
    start: Pair<Int, Int>,
    end: Pair<Int, Int>,
    gridState: Array<Array<Boolean>>
): Int? {
    val (startRow, startCol) = start
    val (endRow, endCol) = end

    // Direcciones de movimiento permitidas (horizontal y vertical)
    val directions = listOf(
        Pair(-1, 0), // Arriba
        Pair(1, 0),  // Abajo
        Pair(0, -1), // Izquierda
        Pair(0, 1)   // Derecha
    )

    // Cola para la búsqueda en anchura (BFS)
    val queue: LinkedList<Triple<Int, Int, Int>> = LinkedList() // (fila, columna, distancia)
    queue.add(Triple(startRow, startCol, 0))

    // Conjunto para rastrear casillas visitadas
    val visited = mutableSetOf<Pair<Int, Int>>()
    visited.add(start)

    while (queue.isNotEmpty()) {
        val (currentRow, currentCol, distance) = queue.poll()!!

        // Si alcanzamos la casilla de destino, devolvemos la distancia
        if (currentRow == endRow && currentCol == endCol) {
            return distance
        }

        // Exploramos las casillas adyacentes
        for ((rowOffset, colOffset) in directions) {
            val nextRow = currentRow + rowOffset
            val nextCol = currentCol + colOffset

            // Verificamos si la casilla está dentro del grid y no ha sido visitada
            if (nextRow in gridState.indices &&
                nextCol in gridState[0].indices &&
                gridState[nextRow][nextCol] &&
                Pair(nextRow, nextCol) !in visited
            ) {
                // Agregamos la casilla a la cola y marcamos como visitada
                queue.add(Triple(nextRow, nextCol, distance + 1))
                visited.add(Pair(nextRow, nextCol))
            }
        }
    }

    // Si no encontramos un camino, devolvemos null
    return null
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