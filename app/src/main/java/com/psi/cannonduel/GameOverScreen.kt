package com.psi.cannonduel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun GameOverScreen(
    player1State: PlayerState,
    player2State: PlayerState,
    onBackToMenuClick: () -> Unit 
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Game Over",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 0.dp)
        )

        Text(
            text = "Player 1 HP: ${player1State.hp}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )

        Text(
            text = "Player 2 HP: ${player2State.hp}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 80.dp)
        )

        Button(
            onClick = onBackToMenuClick,
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Back to Menu", fontSize = 20.sp)
        }
    }
}
