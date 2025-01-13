package com.psi.cannonduel

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
    var selectedGamemode by remember { mutableStateOf("User vs AI") }
    val options = listOf("User vs AI", "AI vs AI", "Training")

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

            // Selectores
            options.forEach { option ->
                RadioOption(text = option,
                    selected = selectedGamemode == option,
                    onSelect = { selectedGamemode = option }
                )
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