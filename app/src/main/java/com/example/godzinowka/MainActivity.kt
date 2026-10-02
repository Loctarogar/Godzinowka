package com.example.godzinowka

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.godzinowka.ui.theme.GodzinowkaTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GodzinowkaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Godzinówka",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

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

    val sekundyPredwczoraj by remember {
        mutableStateOf(sharedPreferences.getInt("suma_sekund_$dataPrzedwczoraj", 0))
    }

    val sekundyWczoraj by remember {
        mutableStateOf(sharedPreferences.getInt("suma_sekund_$dataWczoraj", 0 ))
    }

    var sekundyDzisZapisane by remember {
        mutableIntStateOf(sharedPreferences.getInt("suma_sekund_$dataDzis", 0))
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

    LaunchedEffect(czyPracuje) {
        val formatDnia = SimpleDateFormat("dd.MM", Locale.getDefault())
        while(czyPracuje) {
            delay(1.seconds)

            val terazMs = System.currentTimeMillis()
            val calTeraz = Calendar.getInstance().apply { timeInMillis = terazMs }
            val dataTeraz = formatDnia.format(calTeraz.time)

            val startMs = sharedPreferences.getLong("czas_startu_ms", terazMs)
            val calStart = Calendar.getInstance().apply { timeInMillis = startMs }
            val dataStartu = formatDnia.format(calStart.time)

            if(dataTeraz != dataStartu) {
                val calPolnoc = Calendar.getInstance().apply {
                    timeInMillis = startMs
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val sekundyDoPolnocy = ((calPolnoc.timeInMillis - startMs) / 1000).toInt()
                val dotychczasWczoraj = sharedPreferences.getInt("suma_sekund_$dataStartu", 0)
                sharedPreferences.edit()
                    .putInt("suma_sekund_$dataStartu", dotychczasWczoraj + sekundyDoPolnocy)
                    .apply()

                val poczatekDzisiajMs = calPolnoc.timeInMillis + 1L
                val noweSekundyDzis = ((terazMs - poczatekDzisiajMs) / 1000).toInt()

                sekundy = noweSekundyDzis

                sharedPreferences.edit()
                    .putLong("czas_startu_ms", poczatekDzisiajMs)
                    .putInt("zapisane_sekundy", sekundy)
                    .putString("czas_startu_tekst", pobierzAktualnaDateICzas())
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

                        sekundyDzisZapisane += sekundy
                        czyPracuje = false
                        sekundy = 0
                        czasStartuTekst = ""

                        sharedPreferences.edit()
                            .putInt("suma_sekund_$dataDzis", sekundyDzisZapisane)
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

fun timer(sekundy: Int): String {
    val h = sekundy / 3600
    val m = (sekundy % 3600) / 60
    val s = sekundy % 60
    val sformatowanyCzas = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)

    return sformatowanyCzas
}

fun przeliczZarobek (
    sekundy: Int,
    stawkaStandardowa: Double,
    stawkaNadgodziny:  Double
): Double {

    val pelneKwadranse = sekundy / 900

    val zarobek = if (czyWeekend()) {
        pelneKwadranse * (stawkaNadgodziny / 4.0)
    } else if (pelneKwadranse <= 32) {
        pelneKwadranse * (stawkaStandardowa / 4.0)
    } else {
        val zarobekBaza = 32 * (stawkaStandardowa / 4.0)
        val nadgodzinyKwadranse = pelneKwadranse - 32
        val zarobekNadgodziny = nadgodzinyKwadranse * (stawkaNadgodziny / 4.0)

        zarobekBaza + zarobekNadgodziny
    }

    return zarobek
}

fun czyWeekend(): Boolean {
    val dzienTygodnia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    return dzienTygodnia == Calendar.SATURDAY || dzienTygodnia == Calendar.SUNDAY
}

fun pobierzAktualnaDateICzas(): String {
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())

    return format.format(java.util.Date())
}

fun formatujGodzinyKrotko(sekundy: Int): String {
    val h = sekundy / 3600
    val m = (sekundy % 3600) / 60

    return "${h}g ${m}m"
}

fun zapiszSesjeWHistorii(
    sharedPreferences: android.content.SharedPreferences,
    nowaSesja: SesjaPracy
) {
    val gson = Gson()
    val dotychczasoweSesje = wczytajHistorieSesji(sharedPreferences).toMutableList()

    dotychczasoweSesje.add(0, nowaSesja)

    val jsonTekst = gson.toJson(dotychczasoweSesje)
    sharedPreferences.edit()
        .putString("historia_sesji_json", jsonTekst)
        .apply()
}

fun wczytajHistorieSesji(
    sharedPreferences: android.content.SharedPreferences
): List<SesjaPracy> {
    val gson = Gson()
    val jsonTekst = sharedPreferences.getString("historia_sesji_json", null) ?: return emptyList()

    val typListy = object : TypeToken<List<SesjaPracy>>() {}.type
    return  try {
        gson.fromJson(jsonTekst, typListy)
    } catch (e: Exception) {
        emptyList()
    }
}

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

fun usunSesjeZHistorii (
    sharedPreferences: SharedPreferences,
    indeksDoUsuniecia: Int
) {
    val dotychczasoweSesje = wczytajHistorieSesji(sharedPreferences).toMutableList()
    if (indeksDoUsuniecia in dotychczasoweSesje.indices) {
        dotychczasoweSesje.removeAt(indeksDoUsuniecia)
            val jsonTekst = Gson().toJson(dotychczasoweSesje)
            sharedPreferences.edit()
                .putString("historia_sesji_json", jsonTekst)
                .apply()
    }
}
