package com.psi.cannonduel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Barra de información del jugador
@Composable
fun PlayerBar(
    playerName: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playerName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(150.dp)
                .height(12.dp),
            color = Color.Green,
        )
    }
}

// Grid de juego
@Composable
fun GameGrid(
    gridState: Array<Array<Boolean>>,
    player1Position: Pair<Int, Int>,
    player2Position: Pair<Int, Int>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Pair<Int, Int>) -> Unit
) {
    LazyColumn {
        items(10) { rowIndex ->
            LazyRow {
                items(10) { colIndex ->
                    // Determinar el color de la celda
                    val cellColor = when {
                        player1Position == Pair(rowIndex, colIndex) -> Color.Blue // Jugador
                        player2Position == Pair(rowIndex, colIndex) -> Color.Red // IA
                        selectedCell == Pair(rowIndex, colIndex) -> Color.Yellow // Seleccionada
                        !gridState[rowIndex][colIndex] -> Color.Black // Anulada
                        else -> Color.LightGray // Normal
                    }

                    // Renderizar celda
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .background(cellColor, RoundedCornerShape(4.dp))
                            .clickable { onCellClick(Pair(rowIndex, colIndex)) }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(infoText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Info", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(infoText, fontSize = 16.sp, color = Color.DarkGray)
    }
}

@Composable
fun FuelBar(fuelLevel: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Fuel",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LinearProgressIndicator(
            progress = { fuelLevel },
            modifier = Modifier
                .fillMaxWidth(0.5f) // La barra ocupa el 50% del ancho
                .height(12.dp),
            color = Color.Green,
        )
    }
}

@Composable
fun AmmoSelector(
    selectedAmmo: String,
    onAmmoChange: (String) -> Unit
) {
    Column {
        listOf("Ammo 1", "Ammo 2", "Ammo 3").forEach { ammo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = (selectedAmmo == ammo),
                    onClick = { onAmmoChange(ammo) }
                )
                Text(text = ammo, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ActionButton(
    actionText: String,
    onActionClick: () -> Unit
) {
    Button(
        onClick = onActionClick,
        modifier = Modifier
            .padding(end = 8.dp)
            .height(48.dp)
            .width(120.dp)
    ) {
        Text(actionText, fontSize = 16.sp)
    }
}

@Composable
fun WindInfo(direction: String, strength: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Wind", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Direction: $direction  Strength: $strength", fontSize = 16.sp)
    }
}
