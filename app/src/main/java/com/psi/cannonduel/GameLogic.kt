package com.psi.cannonduel

import androidx.compose.runtime.MutableState

// Función principal para manejar la lógica tras presionar el botón de acción
fun handleActionButtonClick(
    actionText: String,
    selectedCell: Pair<Int, Int>?,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    windDirection: String,
    windStrength: Int,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    // Comprobamos si se ha seleccionado una casilla
    if (selectedCell == null) {
        onInfoUpdate("No cell selected")
        return
    }
    when (actionText) {
        // Si la acción era disparar...
        "Shoot" -> handleShoot(
            selectedCell,
            player1State,
            player2State,
            gridState,
            windDirection,
            windStrength,
            onInfoUpdate,
            onActionChange
        )

        // Si la acción era moverse...
        "Move" -> handleMove(selectedCell, gridState, player1State, onInfoUpdate, onActionChange)
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
    onActionChange: (String) -> Unit
) {
    // Calculamos la casilla golpeada
    val hitCell = calculateHitCell(selectedCell!!, windDirection, windStrength)

    // Si nos golpeamos a nosotros mismos
    if (hitCell == player1State.position) {
        // Nos reducimos la vida
        player1State.hp = (player1State.hp - 1)
        onInfoUpdate("You selected cell ${selectedCell.first},${selectedCell.second} and hit yourself at ${hitCell.first},${hitCell.second}")
        // Si no nos queda vida, se termina la partida
        if (player1State.hp <= 0) {
            onInfoUpdate("You lose")
            return
        }
        // Si golpeamos al enemigo
    } else if (hitCell == player2State.position) {
        // Le reducimos la vida
        player2State.hp = (player2State.hp - 1)
        onInfoUpdate("You selected cell ${selectedCell.first},${selectedCell.second} and hit enemy at ${hitCell.first},${hitCell.second}")
        // Si al rival no le queda vida, se termina la partida
        if (player2State.hp <= 0) {
            onInfoUpdate("You win")
            return
        }
        // Si fallamos, eliminamos la casilla
    } else {
        gridState[hitCell.first][hitCell.second] = false
        onInfoUpdate("You selected cell ${selectedCell.first},${selectedCell.second} and missed at ${hitCell.first},${hitCell.second}")
    }

    // Cambiamos el botón de acción
    onActionChange("Move")
}

// Función para gestionar la lógica de moverse
fun handleMove(
    selectedCell: Pair<Int, Int>,
    gridState: Array<Array<Boolean>>,
    player1State: PlayerState,
    onInfoUpdate: (String) -> Unit,
    onActionChange: (String) -> Unit
) {
    // Comprobamos si la casilla está disponible o destruída
    if (!gridState[selectedCell.first][selectedCell.second]) {
        onInfoUpdate("The selected cell is destroyed")
        return
    }

    // Calculamos la distancia a la casilla seleccionada
    val distance = calculateDistance(player1State.position, selectedCell)

    // Comprobamos si hay suficiente combustible
    if (player1State.fuel < distance) {
        onInfoUpdate("Not enough fuel to move to cell ${selectedCell.first},${selectedCell.second}. Required: $distance, Available: ${player1State.fuel}")
        return
    }

    // Actualizamos el estado del jugador
    player1State.position = selectedCell
    player1State.fuel -= distance
    onInfoUpdate("Moved to ${selectedCell.first},${selectedCell.second}")

    // Cambiamos el botón de acción
    onActionChange("Shoot")
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
    return kotlin.math.max(rowDistance, colDistance)
}


// Función para actualizar el texto de la caja de información
fun updateInfoMessage(infoMessage: MutableState<String>, message: String) {
    infoMessage.value = message
}