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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Función para la pantalla de juego
@Composable
fun GameScreen() {
    // Posiciones iniciales de los jugadores
    val player1Position = remember { mutableStateOf(Pair(9, 9)) } // Jugador (abajo derecha)
    val player2Position = remember { mutableStateOf(Pair(0, 0)) } // IA (arriba izquierda)

    // Estado para la casilla seleccionada
    val selectedCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Barra superior: Player 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Player 2",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { 0.7f },
                color = Color.Green,
                modifier = Modifier
                    .width(150.dp)
                    .height(12.dp)
            )
        }

        // Centro: Grid 10x10 y Caja de Información
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título del viento
            Text("Wind", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Direction: N  Strength: 1", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Grid 10x10
            LazyColumn {
                items(10) { rowIndex ->
                    LazyRow {
                        items(10) { colIndex ->
                            // Determinar el color de la celda
                            val cellColor = when {
                                player1Position.value == Pair(
                                    rowIndex,
                                    colIndex
                                ) -> Color.Blue // Jugador 1
                                player2Position.value == Pair(
                                    rowIndex,
                                    colIndex
                                ) -> Color.Red // IA
                                selectedCell.value == Pair(
                                    rowIndex,
                                    colIndex
                                ) -> Color.Yellow // Casilla seleccionada
                                else -> Color.LightGray // Casilla normal
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                    .background(cellColor, RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedCell.value = Pair(rowIndex, colIndex)
                                    }// Actualizar selección
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caja de información
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Info", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("- Choose a target", fontSize = 16.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de combustible
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fuel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LinearProgressIndicator(
                    progress = { 0.6f }, // Placeholder: Combustible al 60%
                    color = Color.Green,
                    modifier = Modifier
                        .fillMaxWidth(0.5f) // La barra ocupa el 50% del ancho
                        .height(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Espaciado antes del selector de munición y botón
        }

        // Selector de munición y botón de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selector de munición (radio buttons)
            Column {
                val selectedAmmo = remember { mutableStateOf("Ammo 1") }
                listOf("Ammo 1", "Ammo 2", "Ammo 3").forEach { ammo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedAmmo.value == ammo),
                            onClick = { selectedAmmo.value = ammo }
                        )
                        Text(text = ammo, fontSize = 14.sp)
                    }
                }
            }

            // Botón de acción (Shoot)
            Button(
                onClick = { /* Acción del botón */ },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(48.dp)
                    .width(120.dp)
            ) {
                Text("Shoot", fontSize = 16.sp)
            }
        }

        // Barra inferior: Player 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Player 1",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { 1.0f },
                color = Color.Green,
                modifier = Modifier
                    .width(150.dp)
                    .height(12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGameScreen() {
    GameScreen()
}