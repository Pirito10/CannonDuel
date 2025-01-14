package com.psi.cannonduel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Función para la pantalla de selección de dificultad
@Composable
fun TrainingScreen(onNextClick: (Int) -> Unit) {
    val numberOfGames = remember { mutableFloatStateOf(100f) }

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

        // Contenedor central para el slider y botón
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Select amount of games to play", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Slider para seleccionar el número de partidas
            Slider(
                value = numberOfGames.floatValue,
                onValueChange = { numberOfGames.floatValue = it },
                valueRange = 1f..1000f,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto con el valor actual
            Text(
                text = "Games: ${numberOfGames.floatValue.toInt()}",
                fontSize = 20.sp
            )
        }
        // Botón para saltar a la siguiente pantalla
        Button(
            onClick = { onNextClick(numberOfGames.floatValue.toInt()) },
            contentPadding = PaddingValues(horizontal = 60.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(text = "Next", fontSize = 30.sp)
        }
    }
}