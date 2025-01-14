package com.psi.cannonduel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
fun PlayerBar(playerName: String, progress: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre del jugador
        Text(
            text = playerName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        // Contenedor para la barra de vida y su texto
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(28.dp)
        ) {
            // Barra de vida
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = Color.Green,
            )
            // Texto sobre la barra
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// Información del viento
@Composable
fun WindInfo(direction: String, strength: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Wind", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Direction: $direction  Strength: $strength", fontSize = 20.sp)
    }
}

// Grid de juego
@Composable
fun GameGrid(
    gridSize: Int,
    gridState: Array<Array<Boolean>>,
    player1Position: Pair<Int, Int>,
    player2Position: Pair<Int, Int>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Pair<Int, Int>) -> Unit
) {
    // Columnas
    LazyColumn {
        items(gridSize) { rowIndex ->
            // Filas
            LazyRow {
                items(gridSize) { colIndex ->
                    // Color de la celda
                    val cellColor = when {
                        player1Position == Pair(rowIndex, colIndex) -> Color.Blue // Jugador
                        player2Position == Pair(rowIndex, colIndex) -> Color.Red // IA
                        selectedCell == Pair(
                            rowIndex,
                            colIndex
                        ) -> Color.Yellow // Casilla seleccionada
                        !gridState[rowIndex][colIndex] -> Color.Black // Destruída
                        else -> Color.LightGray // Normal
                    }

                    // Celdas
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .background(cellColor, RoundedCornerShape(4.dp))
                            .clickable { onCellClick(Pair(rowIndex, colIndex)) }
                    )
                }
            }
        }
    }
}

// Caja de información
@Composable
fun InfoBox(infoText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            "Info",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Text(infoText, fontSize = 16.sp, color = Color.DarkGray)
    }
}

// Barra de combustible
@Composable
fun FuelBar(fuelLevel: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Fuel",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Contenedor para la barra y el texto
        Box(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .height(28.dp)
        ) {
            // Barra de combustible
            LinearProgressIndicator(
                progress = { fuelLevel },
                modifier = Modifier.fillMaxSize(),
                color = Color.Green,
            )
            // Texto del porcentaje
            Text(
                text = "${(fuelLevel * 100).toInt()}%",
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// Selector de munición
@Composable
fun AmmoSelector(
    selectedAmmo: String,
    ammoCounts: Map<String, Int>,
    onAmmoChange: (String) -> Unit
) {
    // Si no queda munición del tipo seleccionado, deseleccionamos el selector
    if (ammoCounts[selectedAmmo] == 0) {
        onAmmoChange("")
    }

    Column {
        ammoCounts.forEach { (ammo, count) ->
            RadioOption(
                "$ammo: $count",
                (selectedAmmo == ammo),
                count > 0
            ) { if (count > 0) onAmmoChange(ammo) }
        }
    }
}

// Botón de acción
@Composable
fun ActionButton(actionText: String, onActionClick: () -> Unit) {
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

// Elemento para selectores
@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onSelect: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelect, enabled = enabled)
        Text(text = text, fontSize = 20.sp)
    }
}