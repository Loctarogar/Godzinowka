package com.example.godzinowka

import android.content.SharedPreferences
import com.example.godzinowka.SesjaPracy
import com.example.godzinowka.model.PodsumowanieOkresu
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

fun obliczSekundyDlaDnia (sesje: List<SesjaPracy>, dataFormatDdMm: String): Int {
    return sesje.filter { sesja ->
        sesja.dataCzasStartu.startsWith(dataFormatDdMm)
    }.sumOf { it.przepracowaneSekundy }
}

fun filtrujSesjeZaOkres(
    sesje: List<SesjaPracy>,
    dniWstecz: Int,
    sharedPreferences: SharedPreferences,
    biezaceSekundy: Int = 0,
    czyTerazPracuje: Boolean = false
): PodsumowanieOkresu {
    val teraz = Calendar.getInstance()
    val calGranica = Calendar.getInstance().apply {
        if (dniWstecz ==1 ) {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        } else {
            add(Calendar.DAY_OF_YEAR, -dniWstecz)
        }
    }
//    teraz.add(Calendar.DAY_OF_YEAR, - dniWstecz)
    val granicznaData = calGranica.time

    val formatDaty = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formatDnia = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dzisiajTekst = formatDnia.format(teraz.time)

    val stawkaStd = sharedPreferences.getString("stawka_standardowa", "30.0")?.toDoubleOrNull() ?: 30.0
    val stawkaNad = sharedPreferences.getString("stawka_nadgodziny", "45.0")?.toDoubleOrNull() ?: 45.0

    val przefiltrowane = sesje.filter { sesja ->
        try {
            if (dniWstecz == 1){
                sesja.dataCzasStartu.startsWith(dzisiajTekst.substring(0, 5)) ||
                        sesja.dataCzasStopu.startsWith(dzisiajTekst.substring(0, 5))
            } else {
                val dataStartu = formatDaty.parse(sesja.dataCzasStartu)
                dataStartu != null && dataStartu.after(granicznaData)
            }
        } catch (e: Exception) {
            false
        }
    }.toMutableList()

    if (czyTerazPracuje && biezaceSekundy > 0) {
        val terazTekst = formatDaty.format(teraz.time)
        przefiltrowane.add(
            SesjaPracy(
                dataCzasStartu = terazTekst,
                dataCzasStopu = terazTekst,
                przepracowaneSekundy = biezaceSekundy,
                zarobekKwota = 0.0
            )
        )
    }

    var sumaSekundStd = 0
    var sumaZarobkuStd = 0.0
    var sumaSekundNad = 0
    var sumaZarobkuNad = 0.0

    for (sesja in przefiltrowane) {
        val sekundy = sesja.przepracowaneSekundy
        val pelneKwadranse = sekundy / 900

        val cal = Calendar.getInstance()
        var czyWeekendSesji = false
        try {
            val dataStartu = formatDaty.parse(sesja.dataCzasStartu)
            if (dataStartu != null) {
                cal.time = dataStartu
                val day = cal.get(Calendar.DAY_OF_WEEK)
                czyWeekendSesji = (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
            }
        } catch (_: Exception) {}

        if (czyWeekendSesji) {
            sumaSekundNad += sekundy
            sumaZarobkuNad += pelneKwadranse * (stawkaNad / 4.0)
        } else {
            if (pelneKwadranse <= 32){
                sumaSekundStd += sekundy
                sumaZarobkuStd += pelneKwadranse * (stawkaStd / 4.0)
            } else {
                val sekundyBaza = 32 * 900
                val sekundyNad = sekundy - sekundyBaza
                val kwadranseNad = pelneKwadranse - 32

                sumaSekundStd += sekundyBaza
                sumaZarobkuStd += 32 * (stawkaStd / 4.0)

                sumaSekundNad += sekundyNad
                sumaZarobkuNad += kwadranseNad * (stawkaNad / 4.0)
            }
        }
    }

    val nazwa = when (dniWstecz) {
        1 -> "Dzisiaj"
        7 -> "Bieżący tydzień"
        else -> "Bieżący miesiąc"
    }

    return PodsumowanieOkresu(
        etykieta = nazwa,
        laczneSekundy = sumaSekundStd + sumaSekundNad,
        laczyZarobek = sumaZarobkuStd + sumaZarobkuNad,
        sekundyStandard = sumaSekundStd,
        zarobekStandard = sumaZarobkuStd,
        sekundyNadgodziny = sumaSekundNad,
        zarobekNadgodziny = sumaZarobkuNad,
        stawkaStandard = stawkaStd,
        stawkaNadgodziny = stawkaNad
    )
}