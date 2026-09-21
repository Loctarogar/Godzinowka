package com.example.godzinowka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

    LaunchedEffect(czyPracuje) {
        while (czyPracuje) {
            delay(1000L)
            sekundy++
        }
    }
    Column(modifier = modifier) {
        Text(text = name)
        Text(text = if(czyPracuje) "Status: Praca w toku" else "Status: Zatrzymano" )
        Text(text = "Czas pracy: ${sekundy}")
        Button(onClick = {czyPracuje = !czyPracuje}) {
            Text(text = if (czyPracuje) "Stop" else "Start")
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
