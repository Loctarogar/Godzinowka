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
import com.example.godzinowka.data.wczytajHistorieSesji
import com.example.godzinowka.filtrujSesjeZaOkres
import com.example.godzinowka.formatujGodzinyKrotko
import java.util.Locale

@Composable
fun EkranRaportow (
    sharedPreferences: SharedPreferences,
    biezaceSekundy: Int,
    czyPracuje: Boolean,
    onPowrot: () -> Unit
) {
    val sesje = remember { wczytajHistorieSesji(sharedPreferences) }

    val raportDzisiaj = remember(sesje, biezaceSekundy, czyPracuje) {
        filtrujSesjeZaOkres(sesje, 1, sharedPreferences, biezaceSekundy, czyPracuje)
    }
    val raportTydzien = remember(sesje, biezaceSekundy, czyPracuje) {
        filtrujSesjeZaOkres(sesje, 7, sharedPreferences, biezaceSekundy, czyPracuje)
    }
    val raportMiesiac = remember(sesje, biezaceSekundy, czyPracuje) {
        filtrujSesjeZaOkres(sesje, 30, sharedPreferences, biezaceSekundy, czyPracuje)
    }

    val listaRaportow = listOf(raportDzisiaj, raportTydzien, raportMiesiac)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp,bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Raporty okresowe", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listaRaportow.size) { indeks ->
                val raport = listaRaportow[indeks]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = raport.etykieta,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Łącznie: ${formatujGodzinyKrotko(raport.laczneSekundy)}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f zł", raport.laczyZarobek),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Baza (${String.format(Locale.getDefault(), "%.0f", raport.stawkaStandard)} zł/h): ${formatujGodzinyKrotko(raport.sekundyStandard)}",
                                fontSize = 13.sp
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f zł", raport.zarobekStandard),
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• Nadgodziny (${String.format(Locale.getDefault(), "%.0f", raport.stawkaNadgodziny)} zł/h): ${formatujGodzinyKrotko(raport.sekundyNadgodziny)}",
                                fontSize = 13.sp
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f zł", raport.zarobekNadgodziny),
                                fontSize = 13.sp
                            )
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