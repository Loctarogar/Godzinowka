package com.example.godzinowka.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EkranOpcji(
    stawkaStandardowa: String,
    onStawkaStandardowaChange: (String) -> Unit,
    stawkaNadgodziny: String,
    onStawkaNadgodzinyChange: (String) -> Unit,
    onPowrot: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Ustawienia stawek", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = stawkaStandardowa,
            onValueChange = onStawkaStandardowaChange,
            label = { Text("Stawka bazowa (zł/h)") }
        )

        Spacer(modifier = Modifier.height( 8.dp ))

        OutlinedTextField(
            value = stawkaNadgodziny,
            onValueChange = onStawkaNadgodzinyChange,
            label = { Text("Stawka nadgodziny/weekend (zł/h)")}
        )

        Spacer(modifier =  Modifier.height(24.dp))

        Button(onClick = onPowrot) {
            Text(text = "Wróć")
        }
    }
}