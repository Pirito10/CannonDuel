package com.psi.cannonduel

import androidx.compose.runtime.MutableState
import com.chaquo.python.PyObject
import java.util.LinkedList

// Función para manejar la lógica del botón de acción
fun handleActionButtonClick(
    player: String,
    difficulty: String,
    actionText: String,
    selectedCell: Pair<Int, Int>?,
    selectedAmmo: MutableState<String>,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>,
    knownWindDirection: MutableState<String>,
    knownWindStrength: MutableState<Int>,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onClearSelection: () -> Unit,
    onGameOver: () -> Unit,
    pythonModule: PyObject
) {
    if (player == "AI") {
        while (!checkGameOver(player1State, player2State)) {
            handleMediumAI(
                player1State,
                player2State,
                gridState,
                knownWindDirection.value,
                knownWindStrength.value,
                onGameOver,
                pythonModule
            )

            // Verificamos si el juego termina después del turno del jugador 1
            if (checkGameOver(player1State, player2State)) break

            // Turno del jugador 2 (IA)
            handleMediumAI(
                player2State,
                player1State, // Intercambiamos el orden de los estados
                gridState,
                knownWindDirection.value,
                knownWindStrength.value,
                onGameOver,
                pythonModule
            )

            updateWind(
                windDirection,
                windStrength,
                knownWindDirection,
                knownWindStrength
            ) // Actualizamos el viento
        }
    }

    when (actionText) {
        // Si la acción era disparar, gestionamos el disparo
        "Shoot" -> handleShoot(
            selectedCell,
            selectedAmmo,
            player1State,
            player2State,
            gridState,
            windDirection.value,
            windStrength.value,
            onInfoUpdate,
            onActionChange,
            onGameOver
        )

        // Si la acción era moverse, gestionamos el movimiento
        "Move" -> handleMove(
            selectedCell,
            gridState,
            player1State,
            player2State,
            onInfoUpdate,
            onActionChange
        )

        // Si la acción era siguiente turno, gestionamos el turno del rival
        "Next" -> {
            handleNext(
                difficulty,
                player1State,
                player2State,
                gridState,
                windDirection.value,
                windStrength.value,
                onActionChange,
                onGameOver,
                pythonModule
            )
            // Actualizamos el viento
            updateWind(windDirection, windStrength, knownWindDirection, knownWindStrength)
        }
    }

    // Deseleccionamos la casilla
    onClearSelection()
}

// Función para gestionar la lógica de disparar
fun handleShoot(
    selectedCell: Pair<Int, Int>?,
    selectedAmmo: MutableState<String>,
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
    processShot(
        selectedCell,
        selectedAmmo,
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

    // Cambiamos el botón de acción
    onActionChange("Move")
}

// Función para gestionar la lógica de moverse
fun handleMove(
    selectedCell: Pair<Int, Int>?,
    gridState: Array<Array<Boolean>>,
    player1State: PlayerState,
    player2State: PlayerState,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        onInfoUpdate("No cell selected")
        return
    }

    // Procesamos el movimiento
    if (!processMove(selectedCell, player1State, player2State, gridState)) {
        // Si no se pudo realizar el movimiento, no hacemos nada
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
    onGameOver: () -> Unit,
    pythonModule: PyObject
) {
    // Gestionamos el turno del rival según la dificultad seleccionada
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
            onGameOver,
            pythonModule
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
    selectedAmmo: MutableState<String>,
    windDirection: String,
    windStrength: Int,
    shooterState: PlayerState,
    targetState: PlayerState,
    gridState: Array<Array<Boolean>>
) {
    // Reducimos la munición del tipo seleccionado
    val ammoType = selectedAmmo.value
    shooterState.ammo[ammoType] = shooterState.ammo[ammoType]!! - 1

    shooterState.lastKnownPosition = shooterState.position

    when (ammoType) {
        "Standard" -> {
            val damage = 2

            // Calculamos la casilla golpeada
            val hitCell = calculateHitCell(targetCell, windDirection, windStrength)

            when (hitCell) {
                // Si se golpea a sí mismo, le reducimos la vida
                shooterState.position -> {
                    shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si golpea al rival, le reducimos la vida
                targetState.position -> {
                    targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si falla, marcamos la casilla como destruída
                else -> gridState[hitCell.first][hitCell.second] = false
            }
        }

        "Precision" -> {
            val damage = 1

            when (targetCell) {
                // Si se golpea a sí mismo, le reducimos la vida
                shooterState.position -> {
                    shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si golpea al rival, le reducimos la vida
                targetState.position -> {
                    targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si falla, marcamos la casilla como destruída
                else -> gridState[targetCell.first][targetCell.second] = false
            }
        }

        "Nuke" -> {
            val damage = 3

            // Calculamos la casilla golpeada
            val hitCell = calculateHitCell(targetCell, windDirection, windStrength)

            for (rowOffset in -1..1) {
                for (colOffset in -1..1) {
                    val affectedRow = hitCell.first + rowOffset
                    val affectedCol = hitCell.second + colOffset

                    if (affectedRow in gridState.indices && affectedCol in gridState[0].indices) {
                        val affectedCell = Pair(affectedRow, affectedCol)

                        // Si el rival está en la casilla afectada, le reducimos la vida
                        if (affectedCell == targetState.position) {
                            targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                        }
                        // Si el propio jugador está en la casilla afectada, se reduce su vida
                        else if (affectedCell == shooterState.position) {
                            shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                        }
                        // Si no hay jugadores, destruimos la casilla
                        else {
                            gridState[affectedRow][affectedCol] = false
                        }
                    }
                }
            }

            when (hitCell) {
                // Si se golpea a sí mismo, le reducimos la vida
                shooterState.position -> {
                    shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si golpea al rival, le reducimos la vida
                targetState.position -> {
                    targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                    return
                }

                // Si falla, marcamos la casilla como destruída
                else -> gridState[hitCell.first][hitCell.second] = false
            }
        }
    }
}

// Función para procesar un movimiento
fun processMove(
    selectedCell: Pair<Int, Int>,
    playerState: PlayerState,
    opponentState: PlayerState,
    gridState: Array<Array<Boolean>>
): Boolean {
    // Comprobamos que la casilla no sea la del oponente
    if (selectedCell == opponentState.position) {
        return false
    }

    // Comprobamos que la casilla no esté destruída
    if (!gridState[selectedCell.first][selectedCell.second]) {
        return false
    }

    // Calculamos la menor distancia a la casilla seleccionada
    val distance = calculatePathDistance(playerState.position, selectedCell, gridState)

    // Comprobamos que exista un camino válido
    if (distance == null) {
        return false
    }

    // Comprobamos si hay suficiente combustible
    if (playerState.fuel < distance) {
        return false
    }

    // Actualizamos la posición y combustible del jugador
    playerState.position = selectedCell
    playerState.fuel -= distance
    return true
}

// Función para calcular la casilla golpeada en función del viento
fun calculateHitCell(
    selectedCell: Pair<Int, Int>,
    windDirection: String,
    windStrength: Int
): Pair<Int, Int> {
    var (row, col) = selectedCell

    // Modificamos la fila y columna golpeada según la intensidad, y dependiendo de la dirección
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

    // Nos aseguramos de que la casilla no se salga del grid
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

// Función para actualizar el texto de la caja de información
fun updateInfoMessage(infoMessage: MutableState<String>, message: String) {
    infoMessage.value = message
}

// Función para actualizar el viento
fun updateWind(
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>,
    knownWindDirection: MutableState<String>,
    knownWindStrength: MutableState<Int>
) {
    knownWindDirection.value = windDirection.value
    knownWindStrength.value = windStrength.value

    // Lista de posibles direcciones
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    // Máximo valor de intensidad
    val maxStrength = 4

    // Actualizamos la intensidad (entre -1 y 2 unidades)
    val strengthChange = listOf(-1, 0, 1, 2).random()
    windStrength.value = (windStrength.value + strengthChange).coerceIn(0, maxStrength)

    // Actualizamos la dirección (rotando entre -2 y 2 unidades)
    val currentIndex = directions.indexOf(windDirection.value)
    val directionChange = listOf(-2, -1, 0, 1, 2).random()
    val newIndex = (currentIndex + directionChange + directions.size) % directions.size
    windDirection.value = directions[newIndex]
}

// Función para comprobar si se ha terminado la partida
fun checkGameOver(
    player1State: PlayerState,
    player2State: PlayerState
): Boolean {
    // Comprobamos si alguno de los jugadores se ha quedado sin puntos de vida
    if (player1State.hp <= 0 || player2State.hp <= 0) {
        return true
    }

    // Comprobamos si ambos jugadores se han quedado sin munición
    if (player1State.ammo.all { it.value <= 0 } && player2State.ammo.all { it.value <= 0 }) {
        return true
    }

    return false
}

fun getAvailableCells(gridState: Array<Array<Boolean>>): List<Pair<Int, Int>> {
    val availableCells = mutableListOf<Pair<Int, Int>>()
    for (row in gridState.indices) {
        for (col in gridState[row].indices) {
            if (gridState[row][col]) {
                availableCells.add(Pair(row, col))
            }
        }
    }
    return availableCells
}