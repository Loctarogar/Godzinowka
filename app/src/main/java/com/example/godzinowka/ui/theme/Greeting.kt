package com.example.godzinowka.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.godzinowka.SesjaPracy
import com.example.godzinowka.czyWeekend
import com.example.godzinowka.data.wczytajHistorieSesji
import com.example.godzinowka.data.zapiszSesjeWHistorii
import com.example.godzinowka.formatujGodzinyKrotko
import com.example.godzinowka.obliczSekundyDlaDnia
import com.example.godzinowka.pobierzAktualnaDateICzas
import com.example.godzinowka.przeliczZarobek
import com.example.godzinowka.timer
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(
            "UstawieniaGodzinowka",
            android.content.Context.MODE_PRIVATE
        )
    }

    var czyPracuje by remember {
        mutableStateOf( sharedPreferences.getBoolean("stan_czy_pracuje",  false))
    }

    var czasStartuTekst by remember {
        mutableStateOf(sharedPreferences.getString("czas_startu_tekst", "") ?: "")
    }

    var sekundy by remember {
        val czasRozpoczecia = sharedPreferences.getLong("czas_startu_ms", 0L)

        val sekundyPoczatkowe = if (czyPracuje && czasRozpoczecia > 0L) {
            val minelosMs = System.currentTimeMillis() - czasRozpoczecia
            (minelosMs / 1000).toInt()
        } else {
            sharedPreferences.getInt("zapisane_sekundy", 0)
        }

        mutableIntStateOf(sekundyPoczatkowe)
    }

    var stawkaStandardowaText by remember {
        mutableStateOf( sharedPreferences.getString("stawka_standardowa", "30.0") ?:"34.0")
    }
    var stawkaNadgodzinyText by remember {
        mutableStateOf( sharedPreferences.getString("stawka_nadgodziny", "45.0") ?:"45.0")
    }

    val stawkaStandardowa = stawkaStandardowaText.toDoubleOrNull() ?: 0.0
    val stawkaNadgodziny = stawkaNadgodzinyText.toDoubleOrNull() ?: 0.0

    val sdfDnia = SimpleDateFormat("dd.MM", Locale.getDefault())
    val kalendarz = Calendar.getInstance()

    val dataDzis = sdfDnia.format(kalendarz.time)
    kalendarz.add(Calendar.DAY_OF_YEAR, -1)
    val dataWczoraj = sdfDnia.format(kalendarz.time)
    kalendarz.add(Calendar.DAY_OF_YEAR, -1)
    val dataPrzedwczoraj = sdfDnia.format(kalendarz.time)

    val sesjeHistorii = remember(czyPracuje) { wczytajHistorieSesji(sharedPreferences) }

    val sekundyPredwczoraj = remember(sesjeHistorii) {
        obliczSekundyDlaDnia(sesjeHistorii, dataPrzedwczoraj)
    }

    val sekundyWczoraj = remember(sesjeHistorii) {
        obliczSekundyDlaDnia(sesjeHistorii, dataWczoraj)
    }

    val sekundyDzisZapisane = remember(sesjeHistorii) {
        obliczSekundyDlaDnia(sesjeHistorii, dataDzis)
    }

    val sumaDzisZapisane = sekundyDzisZapisane + sekundy

    val zarobek = przeliczZarobek(sumaDzisZapisane, stawkaStandardowa, stawkaNadgodziny)

    val tekstStawki = when {
        czyWeekend() -> "Stawka: Weekendowa"
        sumaDzisZapisane > 28800 -> "Stawka: Nadgodziny"
        else -> "Stawka: Standardowa"
    }

    var czyPokazacOpcje by remember {mutableStateOf(false)}
    var czyPokazacHistorie by remember { mutableStateOf(false) }
    var czyPokazacRaporty by remember { mutableStateOf(false) }

    LaunchedEffect(czyPracuje) {
        val formatDnia = SimpleDateFormat("dd.MM", Locale.getDefault())
        val formatPelny = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        while(czyPracuje) {
            delay(1.seconds)

            val terazMs = System.currentTimeMillis()
            val calTeraz = Calendar.getInstance().apply { timeInMillis = terazMs }
            val dataTeraz = formatDnia.format(calTeraz.time)

            val startMs = sharedPreferences.getLong("czas_startu_ms", terazMs)
            val calStart = Calendar.getInstance().apply { timeInMillis = startMs }
            val dataStartu = formatDnia.format(calStart.time)

            if(dataTeraz != dataStartu) {
                // 1. Wyznaczamy moment północy dla starego dnia (23:59:59)
                val calPolnoc = Calendar.getInstance().apply {
                    timeInMillis = startMs
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val sekundyDoPolnocy = ((calPolnoc.timeInMillis - startMs) / 1000).toInt()
                val czasStopuWczoraj = formatPelny.format(calPolnoc.time)
                val zarobekWczoraj = przeliczZarobek(sekundyDoPolnocy, stawkaStandardowa, stawkaNadgodziny)

                // 2. Tworzymy i zapisujemy zamkniętą sesję starego dnia w historii
                val sesjaWczoraj = SesjaPracy(
                    dataCzasStartu = if(czasStartuTekst.isNotEmpty()) czasStartuTekst else formatPelny.format(calStart.time),
                    dataCzasStopu = czasStopuWczoraj,
                    przepracowaneSekundy = sekundyDoPolnocy,
                    zarobekKwota = zarobekWczoraj
                )
                zapiszSesjeWHistorii(sharedPreferences, sesjaWczoraj)

                // 3. Rozpoczynamy nowy dzień od północy (00:00:00)
                val poczatekDzisiajMs = calPolnoc.timeInMillis + 1L
                val noweSekundyDzis = ((terazMs - poczatekDzisiajMs) / 1000).toInt()

                sekundy = noweSekundyDzis
                czasStartuTekst = formatPelny.format(Calendar.getInstance().apply { timeInMillis = poczatekDzisiajMs }.time)

                sharedPreferences.edit()
                    .putLong("czas_startu_ms", poczatekDzisiajMs)
                    .putInt("zapisane_sekundy", sekundy)
                    .putString("czas_startu_tekst", czasStartuTekst)
                    .apply()
            } else {
                sekundy++
                sharedPreferences.edit().putInt("zapisane_sekundy", sekundy).apply()
            }
        }
    }

    if (czyPokazacOpcje) {
        EkranOpcji (
            stawkaStandardowa = stawkaStandardowaText,
            onStawkaStandardowaChange = { nowaStawka ->
                stawkaStandardowaText = nowaStawka
                sharedPreferences.edit().putString("stawka_standardowa", nowaStawka).apply()
            },
            stawkaNadgodziny = stawkaNadgodzinyText,
            onStawkaNadgodzinyChange = { nowaStawka ->
                stawkaNadgodzinyText = nowaStawka
                sharedPreferences.edit().putString("stawka_nadgodziny", nowaStawka).apply()
            },
            onPowrot = { czyPokazacOpcje = false}
        )
    } else if(czyPokazacHistorie) {
        EkranHistorii(
            sharedPreferences = sharedPreferences,
            onPowrot = {czyPokazacHistorie = false}
        )
    } else if (czyPokazacRaporty) {
        EkranRaportow(
            sharedPreferences = sharedPreferences,
            biezaceSekundy = sekundy,
            czyPracuje = czyPracuje,
            onPowrot = {czyPokazacRaporty = false}
        )
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name, fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tekstStawki,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = dataPrzedwczoraj, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatujGodzinyKrotko(sekundyPredwczoraj), fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = dataWczoraj, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatujGodzinyKrotko(sekundyWczoraj), fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = dataDzis, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatujGodzinyKrotko(sumaDzisZapisane), fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if(czyPracuje) "Status: Praca w toku" else "Status: Zatrzymano",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timer(sekundy),
                fontSize = 48.sp,
                fontWeight =  FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "Zarobek: %.2f zł", zarobek),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button(onClick = {
                    if(!czyPracuje) {
                        val terazTekst = pobierzAktualnaDateICzas()
                        czasStartuTekst = terazTekst
                        czyPracuje = true

                        sharedPreferences.edit()
                            .putBoolean("stan_czy_pracuje", true)
                            .putLong("czas_startu_ms", System.currentTimeMillis())
                            .putString("czas_startu_tekst", terazTekst)
                            .apply()
                    } else {
                        val czasStopuTekst = pobierzAktualnaDateICzas()
                        val nowaSesja = SesjaPracy(
                            dataCzasStartu = if(czasStartuTekst.isNotEmpty()) czasStartuTekst else czasStopuTekst,
                            dataCzasStopu = czasStopuTekst,
                            przepracowaneSekundy = sekundy,
                            zarobekKwota = zarobek
                        )
                        zapiszSesjeWHistorii(sharedPreferences, nowaSesja)

                        czyPracuje = false
                        sekundy = 0
                        czasStartuTekst = ""

                        sharedPreferences.edit()
                            .putBoolean("stan_czy_pracuje", false)
                            .putInt("zapisane_sekundy", 0)
                            .putLong("czas_startu_ms", 0L)
                            .putString("czas_startu_tekst", "")
                            .apply()
                    }
                }) {
                    Text(text = if (czyPracuje) "Stop" else "Start")
                }
            }


            Row {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { czyPokazacHistorie = true}) {
                    Text(text = "Historia")
                }

                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {czyPokazacRaporty = true}){
                    Text(text = "Raporty")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { czyPokazacOpcje = true}) {
                    Text(text = "Opcje")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GodzinowkaTheme {
        Greeting("Android")
    }
}
