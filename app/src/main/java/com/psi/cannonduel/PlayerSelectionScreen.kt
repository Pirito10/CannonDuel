package com.psi.cannonduel

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Función para la pantalla de selección de jugador
@Composable
fun PlayerSelectionScreen(onNextClick: (selectedPlayer: String) -> Unit) {
    // Variable con el tipo de jugador seleccionado (usuario o IA)
    var selectedPlayer by remember { mutableStateOf("User") } // Selección por defecto: usuario

    // Contenedor que ocupa toda la pantalla
    Box(modifier = Modifier.fillMaxSize()) {
        // Título en la parte superior
        Text(
            text = "Cannon Duel",
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // Contenedor para el selector de jugador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {

            Text(text = "Choose who will play", fontSize = 24.sp)

            // Contenedor para el selector de "User"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedPlayer == "User",
                    onClick = { selectedPlayer = "User" })
                Text(text = "User", fontSize = 20.sp)
            }
            // Contenedor para el selector de "AI"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedPlayer == "AI",
                    onClick = { selectedPlayer = "AI" })
                Text(text = "AI", fontSize = 20.sp)
            }
        }

        // Botón para saltar a la siguiente pantalla
        Button(
            onClick = { onNextClick(selectedPlayer) },
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Next", fontSize = 30.sp)
        }
    }
}