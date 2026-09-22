package com.example.godzinowka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.godzinowka.ui.theme.GodzinowkaTheme
import kotlinx.coroutines.delay

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
    var czyPracuje by remember { mutableStateOf(false) }
    var sekundy by remember { mutableStateOf(0) }

    var zarobek = przeliczZarobek(sekundy)

    LaunchedEffect(czyPracuje) {
        while (czyPracuje) {
            delay(1000L)
            sekundy++
        }
    }

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
            text = String.format("Zarobek: %.2f zł", zarobek),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = {czyPracuje = !czyPracuje}) {
                Text(text = if (czyPracuje) "Stop" else "Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                czyPracuje = false
                sekundy = 0
            }) {
                Text(text = "Reset")
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

fun timer(sekundy: Int): String {
    val h = sekundy / 3600
    val m = (sekundy % 3600) / 60
    val s = sekundy % 60
    val sformatowanyCzas = String.format("%02d:%02d:%02d", h, m, s)

    return sformatowanyCzas;
}

fun przeliczZarobek (sekundy: Int): Double {
    var stawkaGodzinowa = 30.0
    val pelneKwadranse = sekundy / 900
    val zarobek = pelneKwadranse * (stawkaGodzinowa / 4.0)

    return zarobek;
}