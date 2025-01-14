package com.psi.cannonduel

import androidx.compose.runtime.MutableState
import com.chaquo.python.PyObject
import java.util.LinkedList

// Función para manejar la lógica del botón de acción
fun handleActionButtonClick(
    gamemode: String,
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
    pythonModule: PyObject,
    onActionChange: (String) -> Unit,
    onClearSelection: () -> Unit,
    onGameOver: (PlayerState, PlayerState) -> Unit
) {
    if (gamemode == "User vs AI") {
        runUserGame(
            difficulty,
            actionText,
            selectedCell,
            selectedAmmo,
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            knownWindDirection,
            knownWindStrength,
            onActionChange,
            onGameOver,
            pythonModule
        )
    } else if (gamemode == "AI vs AI") {
        runAIGame(
            difficulty,
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            knownWindDirection,
            knownWindStrength,
            onGameOver,
            pythonModule
        )
    }

    // Deseleccionamos la casilla
    onClearSelection()
}

// Función para jugar una partida usuario vs IA
fun runUserGame(
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
    onActionChange: (String) -> Unit,
    onGameOver: (PlayerState, PlayerState) -> Unit,
    pythonModule: PyObject
) {
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
            onActionChange,
            onGameOver
        )

        // Si la acción era moverse, gestionamos el movimiento
        "Move" -> handleMove(
            selectedCell,
            gridState,
            player1State,
            player2State,
            onActionChange
        )

        // Si la acción era siguiente turno, gestionamos el turno del rival
        "Next" -> {
            handleNext(
                difficulty,
                player2State,
                player1State,
                gridState,
                windDirection.value,
                windStrength.value,
                knownWindDirection.value,
                knownWindStrength.value,
                pythonModule,
                onActionChange,
                onGameOver
            )
            // Actualizamos el viento
            updateWind(windDirection, windStrength, knownWindDirection, knownWindStrength)
        }
    }
}

// Función para jugar una partida IA vs IA
fun runAIGame(
    difficulty: String,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>,
    knownWindDirection: MutableState<String>,
    knownWindStrength: MutableState<Int>,
    onGameOver: (PlayerState, PlayerState) -> Unit,
    pythonModule: PyObject
) {
    // Ejecutamos turnos en bucle hasta que termine la partida
    while (!checkGameOver(player1State, player2State)) {
        // Turno del jugador 1
        if (difficulty == "Random") {
            handleRandomAI(
                player1State,
                player2State,
                gridState,
                windDirection.value,
                windStrength.value,
                onGameOver
            )
        } else {
            handleNormalAI(
                player1State,
                player2State,
                gridState,
                windDirection.value,
                windStrength.value,
                knownWindDirection.value,
                knownWindStrength.value,
                onGameOver,
                pythonModule
            )
        }

        // Verificamos si terminó la partida
        if (checkGameOver(player1State, player2State)) break

        // Turno del jugador 2
        if (difficulty == "Random") {
            handleRandomAI(
                player2State,
                player1State,
                gridState,
                windDirection.value,
                windStrength.value,
                onGameOver
            )
        } else {
            handleNormalAI(
                player2State,
                player1State,
                gridState,
                windDirection.value,
                windStrength.value,
                knownWindDirection.value,
                knownWindStrength.value,
                onGameOver,
                pythonModule
            )
        }

        // Actualizamos el viento
        updateWind(
            windDirection,
            windStrength,
            knownWindDirection,
            knownWindStrength
        )
    }
}

// Función para gestionar la lógica de disparar
fun handleShoot(
    selectedCell: Pair<Int, Int>?,
    selectedAmmo: MutableState<String>,
    shooterState: PlayerState,
    targetState: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onActionChange: (String) -> Unit,
    onGameOver: (PlayerState, PlayerState) -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        return
    }

    // Comprobamos si se ha seleccionado una munición
    if (selectedAmmo.value.isEmpty()) {
        return
    }

    // Procesamos el disparo
    processShot(
        selectedCell,
        selectedAmmo,
        windDirection,
        windStrength,
        shooterState,
        targetState,
        gridState
    )

    // Comprobamos si se terminó la partida
    if (checkGameOver(shooterState, targetState)) {
        onGameOver(shooterState, targetState)
    }

    // Cambiamos el botón de acción
    onActionChange("Move")
}

// Función para gestionar la lógica de moverse
fun handleMove(
    selectedCell: Pair<Int, Int>?,
    gridState: Array<Array<Boolean>>,
    playerState: PlayerState,
    enemyState: PlayerState,
    onActionChange: (String) -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        return
    }

    // Procesamos el movimiento y comprobamos que se haya realizado
    if (!processMove(selectedCell, playerState, enemyState, gridState)) {
        return
    }

    // Cambiamos el botón de acción
    onActionChange("Next")
}

// Función para gestionar la lógica de siguiente turno
fun handleNext(
    difficulty: String,
    playerState: PlayerState,
    enemyState: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    knownWindDirection: String,
    knownWindStrength: Int,
    pythonModule: PyObject,
    onActionChange: (String) -> Unit,
    onGameOver: (PlayerState, PlayerState) -> Unit
) {
    // Gestionamos el turno del rival según la dificultad seleccionada
    when (difficulty) {
        "Random" -> handleRandomAI(
            playerState,
            enemyState,
            gridState,
            windDirection,
            windStrength,
            onGameOver
        )

        "Normal" -> handleNormalAI(
            playerState,
            enemyState,
            gridState,
            windDirection,
            windStrength,
            knownWindDirection,
            knownWindStrength,
            onGameOver,
            pythonModule
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
): Boolean {
    // Reducimos la munición del tipo seleccionado
    val ammoType = selectedAmmo.value
    shooterState.ammo[ammoType] = shooterState.ammo[ammoType]!! - 1

    // Actualizamos la última posición conocida
    shooterState.lastKnownPosition = shooterState.position

    // Gestionamos el disparo según el tipo de munición
    when (ammoType) {
        "Standard" -> {
            val damage = 2

            // Calculamos la casilla golpeada en función del viento
            when (val hitCell = calculateHitCell(targetCell, windDirection, windStrength)) {
                // Si se golpea a sí mismo, le reducimos la vida
                shooterState.position -> {
                    shooterState.previousHp = shooterState.hp
                    shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                    return false
                }

                // Si golpea al rival, le reducimos la vida
                targetState.position -> {
                    targetState.previousHp = targetState.hp
                    targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                    return true
                }

                // Si falla, marcamos la casilla como destruída
                else -> {
                    gridState[hitCell.first][hitCell.second] = false
                    return false
                }
            }
        }

        "Precision" -> {
            val damage = 1

            when (targetCell) {
                // Si se golpea a sí mismo, le reducimos la vida
                shooterState.position -> {
                    shooterState.previousHp = shooterState.hp
                    shooterState.hp = (shooterState.hp - damage).coerceAtLeast(0)
                    return false
                }

                // Si golpea al rival, le reducimos la vida
                targetState.position -> {
                    targetState.previousHp = targetState.hp
                    targetState.hp = (targetState.hp - damage).coerceAtLeast(0)
                    return true
                }

                // Si falla, marcamos la casilla como destruída
                else -> {
                    gridState[targetCell.first][targetCell.second] = false
                    return false
                }
            }
        }

        "Nuke" -> {
            val damage = 3

            // Calculamos la casilla golpeada en función del viento
            val hitCell = calculateHitCell(targetCell, windDirection, windStrength)

            var enemyHit = false

            // Eliminamos todas las casillas adyacentes
            for (rowOffset in -1..1) {
                for (colOffset in -1..1) {
                    val affectedRow = hitCell.first + rowOffset
                    val affectedCol = hitCell.second + colOffset

                    if (affectedRow in gridState.indices && affectedCol in gridState[0].indices) {
                        val affectedCell = Pair(affectedRow, affectedCol)

                        when (affectedCell) {
                            // Si el propio jugador está en la casilla afectada, le reducimos la vida
                            shooterState.position -> {
                                shooterState.previousHp = shooterState.hp
                                shooterState.hp =
                                    (shooterState.hp - damage).coerceAtLeast(0)
                            }

                            // Si el rival está en una casilla afectada, le reducimos la vida
                            targetState.position -> {
                                targetState.previousHp = targetState.hp
                                targetState.hp =
                                    (targetState.hp - damage).coerceAtLeast(0)
                                enemyHit = true
                            }

                            // Si no hay jugadores, destruímos la casilla
                            else -> {
                                gridState[affectedRow][affectedCol] = false
                            }
                        }
                    }
                }
            }
            return enemyHit
        }
    }
    return false
}

// Función para procesar un movimiento
fun processMove(
    selectedCell: Pair<Int, Int>,
    playerState: PlayerState,
    enemyState: PlayerState,
    gridState: Array<Array<Boolean>>
): Boolean {
    // Comprobamos que la casilla no sea la del oponente
    if (selectedCell == enemyState.position) {
        return false
    }

    // Calculamos la menor distancia a la casilla seleccionada y comprobamos que exista un camino válido
    val distance = calculatePathDistance(playerState.position, selectedCell, gridState)
        ?: return false

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

// Función para actualizar el viento
fun updateWind(
    windDirection: MutableState<String>,
    windStrength: MutableState<Int>,
    knownWindDirection: MutableState<String>,
    knownWindStrength: MutableState<Int>
) {
    // Actualizamos la última información del viento conocida
    knownWindDirection.value = windDirection.value
    knownWindStrength.value = windStrength.value

    // Lista de posibles direcciones y máxima intensidad
    val directions = listOf("N", "E", "S", "W")
    val maxStrength = 2

    // Actualizamos la intensidad (entre -1 y 1 unidades)
    val strengthChange = listOf(-1, 0, 1).random()
    windStrength.value = (windStrength.value + strengthChange).coerceIn(0, maxStrength)

    // Actualizamos la dirección (rotando entre -1 y 1 unidades)
    val currentIndex = directions.indexOf(windDirection.value)
    val directionChange = listOf(-1, 0, 1).random()
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