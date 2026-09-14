# Godzinowka
Aplikacja Android do rejestrowania czasu pracy z podglądem na żywo dla kierownika i automatycznym naliczaniem wypłat.
# Godzinówka ⏱️

Aplikacja mobilna na system Android służąca do precyzyjnego rejestrowania czasu pracy z podglądem na żywo dla kierownika oraz automatycznym naliczaniem wynagrodzenia.

---

## 📱 Główne funkcjonalności

### 👷 Widok Pracownika
* **START / STOP** – proste uruchamianie i zatrzymywanie czasu pracy jednym kliknięciem.
* **Zegar na żywo** – licznik informujący o bieżącym czasie sesji oraz aktualnym progu stawki (standardowa vs nadgodziny).
* **Podgląd zarobków** – automatyczne zestawienie zarobków za dzień, tydzień, miesiąc lub okres trwania umowy.

### 👔 Widok Kierownika
* **Status na żywo** – stały podgląd, czy dany pracownik aktualnie pracuje (np. *„W pracy od 08:00”*).
* **Raporty finansowe** – zestawienie sumy godzin bazowych, nadgodzin, pracy w weekendy oraz łącznej kwoty do wypłaty.
* **Konfiguracja stawek** – możliwość zdefiniowania okresu umowy oraz stawek godzinowych (zł/h).

---

## 💰 Reguły naliczania wynagrodzenia

1. **Stawka bazowa:**
   * Pierwsze 8 godzin pracy w dni powszednie (od poniedziałku do piątku).
2. **Stawka podwyższona (nadgodziny + weekendy):**
   * Każda godzina powyżej 8 godzin w dni powszednie.
   * Wszystkie godziny przepracowane w soboty i niedziele (od pierwszej minuty).

---

## 🛠️ Stack technologiczny

* **Platforma:** Android
* **Język programowania:** Kotlin
* **Interfejs graficzny (UI):** Jetpack Compose
* **Baza danych i chmura:** Google Firebase (Firestore do synchronizacji danych w czasie rzeczywistym)
