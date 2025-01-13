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

// Función para la pantalla de selección de dificultad
@Composable
fun DifficultySelectionScreen(onNextClick: (selectedDifficulty: String) -> Unit) {
    var selectedDifficulty by remember { mutableStateOf("Normal") }
    val difficulties = listOf("Random", "Normal")

    // Contenedor que ocupa toda la pantalla
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Título en la parte superior
        Text(
            text = "Cannon Duel",
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // Contenedor para el selector de dificultad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Select difficulty", fontSize = 24.sp)

            // Selectores
            difficulties.forEach { difficulty ->
                RadioOption(
                    text = difficulty,
                    selected = selectedDifficulty == difficulty,
                    onSelect = { selectedDifficulty = difficulty }
                )
            }
        }

        // Botón para saltar a la siguiente pantalla
        Button(
            onClick = { onNextClick(selectedDifficulty) },
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Next", fontSize = 30.sp)
        }
    }
}