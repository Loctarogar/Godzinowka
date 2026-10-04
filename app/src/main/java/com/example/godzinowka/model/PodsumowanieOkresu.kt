package com.example.godzinowka.model

data class PodsumowanieOkresu (
    val etykieta: String,
    val laczneSekundy: Int,
    val laczyZarobek: Double,
    val sekundyStandard: Int,
    val zarobekStandard: Double,
    val sekundyNadgodziny: Int,
    val zarobekNadgodziny: Double,
    val stawkaStandard: Double,
    val stawkaNadgodziny: Double
)
