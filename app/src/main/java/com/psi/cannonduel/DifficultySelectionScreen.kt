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

// Función para la pantalla de selección de dificultad
@Composable
fun DifficultySelectionScreen(onNextClick: () -> Unit) {
    // Variable con la dificultad seleccionada (fácil, medio o difícil)
    var selectedDifficulty by remember { mutableStateOf("Medium") } // Selección por defecto: medio

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

            // Contenedor para el selector de "Easy"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedDifficulty == "Easy",
                    onClick = { selectedDifficulty = "Easy" })
                Text(text = "Easy", fontSize = 20.sp)
            }
            // Contenedor para el selector de "Medium"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedDifficulty == "Medium",
                    onClick = { selectedDifficulty = "Medium" })
                Text(text = "Medium", fontSize = 20.sp)
            }
            // Contenedor para el selector de "Hard"
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedDifficulty == "Hard",
                    onClick = { selectedDifficulty = "Hard" })
                Text(text = "Hard", fontSize = 20.sp)
            }
        }

        // Botón para saltar a la siguiente pantalla
        Button(
            onClick = { onNextClick() },
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Next", fontSize = 30.sp)
        }
    }
}