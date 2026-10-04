package com.example.godzinowka.ui.theme

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.godzinowka.SesjaPracy
import com.example.godzinowka.data.usunSesjeZHistorii
import com.example.godzinowka.data.wczytajHistorieSesji
import com.example.godzinowka.formatujGodzinyKrotko
import java.util.Locale

@Composable
fun EkranHistorii(
    sharedPreferences: SharedPreferences,
    onPowrot: () -> Unit
) {
    val listaSesji = remember {
        androidx.compose.runtime.mutableStateListOf<SesjaPracy>().apply {
            addAll(wczytajHistorieSesji(sharedPreferences))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Historia sesji",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (listaSesji.isEmpty()){
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Brak zapisanych sesji")
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaSesji.size) {indeks ->
                    val sesja = listaSesji[indeks]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Start: ${sesja.dataCzasStartu}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Stop: ${sesja.dataCzasStopu}",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Czas: ${formatujGodzinyKrotko(sesja.przepracowaneSekundy)}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f zł", sesja.zarobekKwota),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = {
                                        usunSesjeZHistorii(sharedPreferences, indeks)
                                        listaSesji.removeAt(indeks)
                                    }
                                ) {
                                    Text(text = "Usuń")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onPowrot) {
            Text(text = "Wróć")
        }
    }
}