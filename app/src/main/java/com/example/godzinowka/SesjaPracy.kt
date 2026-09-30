package com.example.godzinowka

data class SesjaPracy(
    val dataCzasStartu: String,
    val dataCzasStopu: String,
    val przepracowaneSekundy: Int,
    val zarobekKwota: Double
)
