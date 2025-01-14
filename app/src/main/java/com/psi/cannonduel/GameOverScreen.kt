package com.psi.cannonduel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun GameOverScreen(
    player1State: PlayerState,
    player2State: PlayerState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Game Over",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Player 1 HP: ${player1State.hp}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Player 2 HP: ${player2State.hp}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
