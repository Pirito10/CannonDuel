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

// Función para la pantalla de selección de modo de juego
@Composable
fun GamemodeSelectionScreen(onNextClick: (selectedGamemode: String) -> Unit) {
    // Variable con el modo de juego seleccionado
    var selectedGamemode by remember { mutableStateOf("User vs AI") }

    // Contenedor que ocupa toda la pantalla
    Box(modifier = Modifier.fillMaxSize()) {
        // Título de la aplicación
        Text(
            text = "Cannon Duel",
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // Contenedor para el selector de modo de juego
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Choose game mode", fontSize = 24.sp)

            // Contenedor para el selector de "User vs AI"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGamemode == "User vs AI",
                    onClick = { selectedGamemode = "User vs AI" })
                Text(text = "User vs AI", fontSize = 20.sp)
            }
            // Contenedor para el selector de "AI vs AI"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGamemode == "AI vs AI",
                    onClick = { selectedGamemode = "AI vs AI" })
                Text(text = "AI vs AI", fontSize = 20.sp)
            }

            // Contenedor para el selector de "Training"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGamemode == "Training",
                    onClick = { selectedGamemode = "Training" })
                Text(text = "Training", fontSize = 20.sp)
            }
        }

        // Botón para saltar a la siguiente pantalla
        Button(
            onClick = { onNextClick(selectedGamemode) },
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Next", fontSize = 30.sp)
        }
    }
}