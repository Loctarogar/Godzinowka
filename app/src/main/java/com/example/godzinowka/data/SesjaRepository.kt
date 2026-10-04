package com.example.godzinowka.data

import android.content.SharedPreferences
import com.example.godzinowka.SesjaPracy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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