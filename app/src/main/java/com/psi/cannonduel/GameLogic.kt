package com.psi.cannonduel

fun handleActionButtonClick(
    actionText: String,
    selectedCell: Pair<Int, Int>?,
    player1State: PlayerState,
    player2State: PlayerState,
    gridState: Array<Array<Boolean>>,
    onActionChange: (String) -> Unit
) {
    if (selectedCell == null) return

    if (actionText == "Shoot") {
        if (selectedCell == player2State.position) {
            // Reducir vida del jugador 2
            player2State.hp = (player2State.hp - 1).coerceAtLeast(0)
        } else {
            // Destruir la casilla seleccionada
            gridState[selectedCell.first][selectedCell.second] = false
        }
        onActionChange("Move")
    } else if (actionText == "Move") {
        // Actualizar posición del jugador 1
        player1State.position = selectedCell
        onActionChange("Shoot")
    }
}