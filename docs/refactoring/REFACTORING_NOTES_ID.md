<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Catatan Refactor — Chrona (v0.3.0 → v0.4.0-refactor)

Dokumen ini merangkum refactor yang dilakukan sesuai permintaan: penghapusan
hardcode, penerapan SOLID/Clean Architecture, dan penguatan pemisahan peran
Kotlin/Java/C++.

## 0. Temuan audit awal (jujur, bukan template)

Sebelum refactor, codebase ini **sudah** merupakan proyek hybrid
Kotlin+Java+C++/JNI yang tertata — bukan "kode sangat kotor" secara literal.
Masalah nyata yang ditemukan dan diperbaiki:

1. **God Composable** — `ClockApp.kt` (±240 baris) menyimpan *seluruh* state
   aplikasi (settings, alarm, world clock, timer, stopwatch, navigasi)
   langsung sebagai `remember { mutableStateOf(...) }`, padahal dokumentasi
   proyek sendiri mengklaim ada layer ViewModel — yang ternyata tidak pernah
   diimplementasikan.
2. **Tanpa Dependency Injection** — `SettingsStore`, `AlarmStore`,
   `WorldClockStore`, `AlarmScheduler`, `AlarmSoundPlayer` semuanya
   `object` (singleton statis Kotlin), sehingga sulit diuji dan setiap
   pemanggil harus tahu detail Android framework-nya.
3. **Magic numbers/hardcode** tersebar: durasi timer default `25 * 60`,
   kota favorit `"New York"`, pola getar `longArrayOf(0,500,500)`, interval
   polling `100`/`31`, offset request-code alarm, dsb.
4. **Dead code** — fungsi `handleCommand()` dan komponen `CommandBar`
   didefinisikan tapi tidak pernah dipasang di pohon UI. Ini **tidak saya
   hapus** (kemungkinan fitur yang memang direncanakan menyusul), tapi saya
   beri komentar penjelas di `ClockApp.kt`.
5. **Lapisan native (C++/JNI)** ternyata sudah solid: pakai
   `RegisterNatives` eksplisit, null-check konsisten, `-fno-exceptions`,
   tidak ditemukan segfault. **Tidak ada Secret Key/API Key** di aplikasi
   ini sama sekali — Chrona adalah aplikasi jam offline tanpa pemanggilan
   API apa pun. Modul "secure config" di bawah dibuat sebagai *pola
   siap pakai* untuk secret di masa depan, bukan perbaikan kebocoran yang
   memang tidak ada.

## 1. Penghilangan hardcode → `core/config/AppDefaults.kt`

Semua magic number/seed data dipindah ke satu objek:
`DEFAULT_TIMER_SECONDS`, `SNOOZE_MINUTES`, `SNOOZE_REQUEST_CODE_OFFSET`,
`alarmVibrationPattern()`, `SPRING_DAMPING_RATIO`/`SPRING_FREQUENCY_HZ`,
`DEFAULT_FAVORITE_CITY`, `defaultWorldClocks()`.

## 2. Dependency Injection manual (tanpa Hilt)

Hilt/Dagger **sengaja tidak dipakai** — proyek ini sudah menggabungkan
Gradle + CMake/NDK + 3 bahasa, dan menambah code-gen KSP/KAPT menaikkan
risiko build gagal tanpa saya bisa memverifikasi (sandbox ini tidak
memiliki akses jaringan untuk menjalankan Gradle). Sebagai gantinya:

- `data/SettingsRepository.kt`, `data/AlarmRepository.kt`,
  `data/WorldClockRepository.kt` — interface, dengan implementasi
  `SharedPreferences*` terpisah. Menggantikan `SettingsStore`, `AlarmStore`,
  `WorldClockStore`.
- `alarm/AlarmSchedulerGateway.kt` + `AndroidAlarmScheduler.kt`,
  `alarm/AlarmSoundGateway.kt` + `AndroidAlarmSoundPlayer.kt` — menggantikan
  `AlarmScheduler`/`AlarmSoundPlayer`.
- `di/AppContainer.kt` — *composition root* tunggal, dipegang oleh
  `ClockApplication`. `BroadcastReceiver`/`Activity` (yang diinstansiasi
  Android lewat refleksi, sehingga tidak bisa menerima constructor
  injection) mengambil dependency dari
  `(context.applicationContext as ClockApplication).container` — pola
  *Service Locator* yang jujur didokumentasikan sebagai kompromi, bukan
  "DI murni", karena keterbatasan platform Android itu sendiri.
- `di/AppViewModelFactory.kt` — factory tunggal yang meng-construct semua
  ViewModel dari `AppContainer`.

## 3. Memecah God Composable → 5 ViewModel

`SettingsViewModel`, `AlarmViewModel`, `WorldClockViewModel`,
`TimerViewModel`, `StopwatchViewModel` (di `ui/viewmodel/`) sekarang memegang
semua state dan logika bisnis (persist, reschedule alarm, loop timer via
`viewModelScope`). `ClockApp.kt` sekarang **hanya** merangkai layar dan
menyimpan state UI murni (destinasi, sheet/dialog terbuka) — Single
Responsibility. File `ui/screens/*.kt` **tidak diubah sama sekali** karena
tanda tangan fungsinya sudah menerima nilai polos + lambda, bukan ViewModel
langsung.

Ditambahkan dependency `androidx.lifecycle:lifecycle-viewmodel-compose` dan
`lifecycle-viewmodel-ktx` (versi 2.10.0, sama dengan `lifecycle-runtime-ktx`
yang sudah dipakai) ke `app/build.gradle.kts`.

## 4. Boundary waktu Java + native

`ChronaTimeFormatter` dan `ChronaTimeEngine` menjadi boundary Kotlin bersama untuk format waktu/tanggal, sementara `NativeClock`/`ChronaNativeBridge` menangani primitive native dengan fallback `ClockTimeMath`. Placeholder `chrona_secure_config` dihapus karena tidak mempunyai pemanggil atau secret produksi; repository tidak lagi mengklaim memiliki lapisan secret yang sebenarnya tidak digunakan.

## 5. Perbaikan kecil lain

- `ChronaTimeFormatter.kt`: shared formatting was moved to Kotlin and reads the
  current locale for every call, preventing a stale locale snapshot when the
  system language changes while the process remains alive.
- `NativeClock.kt`: default parameter `springProgress` (`0.85`, `2.6`)
  sekarang merujuk `AppDefaults`.

## File yang dihapus

`data/AlarmStore.kt`, `data/SettingsStore.kt`, `data/WorldClockStore.kt`,
`alarm/AlarmScheduler.kt`, `alarm/AlarmSoundPlayer.kt` — semua digantikan
oleh pasangan interface+implementasi di atas.

## Yang sengaja TIDAK diubah

- `ui/screens/*.kt`, `ui/components/*.kt`, `ui/theme/*` — sudah rapi,
  tidak ada hardcode/SRP yang melanggar signifikan, dan mengubahnya
  menambah risiko tanpa manfaat clean-code yang jelas.
- `ChronaNativeBridge.java`, `chrona_time.cpp/.h` — Java remains the JNI boundary;
  C++ remains native math/ABI metadata. The deterministic fallback is Kotlin
  `ClockTimeMath.kt`.
- String resource `"Snooze 10 min"` di `strings.xml` masih menyimpan angka
  "10" sebagai teks statis (duplikasi kecil dengan `AppDefaults.SNOOZE_MINUTES`).
  Tidak diubah karena menyentuh resource string berarti menyentuh semua
  locale terjemahan — di luar cakupan aman untuk sandbox tanpa build-check.

## Catatan jujur soal verifikasi

Sandbox tempat saya bekerja **tidak punya akses jaringan**, sehingga saya
tidak bisa menjalankan `./gradlew build` untuk memverifikasi kompilasi
end-to-end. Yang sudah saya lakukan sebagai gantinya:

- Grep menyeluruh memastikan tidak ada referensi tersisa ke kelas yang
  dihapus.
- Verifikasi manual setiap field/parameter model (`AlarmItem`,
  `WorldClockItem`, `ClockSettings`) cocok dengan yang dipakai di kode baru.
- Verifikasi tanda tangan setiap Composable layar (`HomeScreen`,
  `AlarmScreen`, dst.) cocok dengan cara `ClockApp.kt` memanggilnya.
- Cek keseimbangan kurung kurawal di semua file yang disentuh.
- Verifikasi semua string resource & drawable yang dipakai di
  `AlarmReceiver.kt` memang ada di `strings.xml`/`res/drawable`.

Tetap disarankan menjalankan build sekali di Android Studio/CI sebelum
rilis, karena ini bukan pengganti kompilasi sungguhan.
