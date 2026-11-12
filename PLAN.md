# Master Plan: MovieTix (Kotlin Multiplatform)

Dokumen ini merupakan perencanaan teknis (*engineering master plan*) untuk aplikasi **MovieTix** — aplikasi pemesanan tiket bioskop modern berbasis **Kotlin Multiplatform (KMP)** dengan antarmuka bersama (*shared UI*) menggunakan **Compose Multiplatform** untuk platform Android dan iOS.

Dokumen ini terintegrasi secara modular dengan dokumen spesifikasi teknis mendalam di direktori `docs/`:
- [docs/ARCHITECTURE.md](file:///Users/ariqhikari/Projects/MovieTix/docs/ARCHITECTURE.md) — Arsitektur Clean Package-by-Layer, Pola UDF/MVI, Kontrak Error Handling, dan Prinsip Expect/Actual.
- [docs/DESIGN_SYSTEM.md](file:///Users/ariqhikari/Projects/MovieTix/docs/DESIGN_SYSTEM.md) — Art Direction "Midnight Cinema & Ticket Craft", palet warna terverifikasi WCAG AA, tipografi, dan spesifikasi komponen.
- [docs/DATA_LAYER.md](file:///Users/ariqhikari/Projects/MovieTix/docs/DATA_LAYER.md) — TMDB API DTO, Procedural Schedule Engine deterministik, Skema DataStore, dan transaksi atomik `ConfirmBookingUseCase`.
- [docs/TASKS.md](file:///Users/ariqhikari/Projects/MovieTix/docs/TASKS.md) — Checklist tugas bertahap (Fase 0 - 6) lengkap dengan saran pesan commit Conventional Commits.

---

## 1. Ringkasan Proyek & Tujuan (Project Goals)

### 1.1 Konteks & Tujuan Portofolio
**MovieTix** dirancang sebagai **proyek portofolio unggulan (*flagship portfolio*)** yang menonjolkan:
1. **Keunggulan Arsitektur**: Penerapan Clean Architecture dengan pemisahan layer `core`, `data`, `domain`, `presentation`, dan `di`. Pengujian unit test common dipermudah melalui mock/fake interface repository.
2. **Karakter Visual Kuat (Anti-AI Slop)**: Menolak estetika klise dan warna gradien generik; mengusung nuansa sinema malam hari (*Obsidian Navy* `#0B0E14` dan *Amber Gold* `#E5A93C`) dengan elemen tiket fisik (lekukan perforasi stempel dan QR voucher offline).
3. **Simulasi Bioskop Realistis & Deterministik**: Menghadirkan *procedural engine* deterministik berbasis hash untuk menghasilkan data bioskop, jam tayang, dan keterisian kursi yang konsisten antar sesi.
4. **100% Shared UI**: Memaksimalkan efisiensi Compose Multiplatform di mana satu codebase UI melayani Android dan iOS tanpa duplikasi SwiftUI.

### 1.2 Ruang Lingkup Fitur (Feature Scope)
- **Katalog Film**: Daftar film *Now Playing*, *Popular*, dan *Upcoming* dari TMDB API dengan pagination infinite scroll, shimmer skeleton, error retry, dan empty state.
- **Detail Film Sinematik**: Backdrop hero, poster tumpang-tindih (corner radius 16dp), sinopsis expandable, durasi, rating ulasan, carousel pemeran (*cast*), dan tautan trailer resmi.
- **Pemesanan Jadwal & Denah Kursi Interaktif**: Pemilih tanggal dinamis, daftar bioskop & tipe studio (IMAX, Velvet, Regular), serta denah kursi interaktif dengan status *Available*, *Selected*, dan *Occupied* (multi-modal: border stroke, glyph centang `v` dan silang `X`).
- **Simulasi Pembayaran (Dummy Payment)**: Ringkasan harga tiket & biaya layanan, pemilihan metode bayar (Virtual Account, E-Wallet, Kartu Kredit), animasi proses transaksi, serta penanganan simulasi berhasil dan gagal.
- **E-Tiket & QR Code Offline**: Tiket bergaya fisik berperforasi (*punched notch*), QR code resolusi tinggi yang dapat dipindai offline via library `qrose`, tersimpan persisten di DataStore.

> **Catatan Ruang Lingkup (Future Work)**: Fitur "Simpan Gambar Tiket ke Galeri", "Tambah ke Kalender Sistem", dan "Bagikan Gambar" dikeluarkan dari scope rilis saat ini agar kode tetap murni multiplatform tanpa ketergantungan plugin native berlebihan.

---

## 2. Analisis Starter Project yang Ada

Berdasarkan inspeksi langsung pada repositori proyek MovieTix:
- **Build System**: Proyek menggunakan **Kotlin Toolchain / Amper** (`project.yaml`, `shared/module.yaml`, `androidApp/module.yaml`, `iosApp/module.yaml`) dengan wrapper CLI `./kotlin`.
- **Target Platform**:
  - `androidApp`: Android Application dengan namespace `org.ariqhikari.movietix`, `enableEdgeToEdge()`.
  - `iosApp`: iOS Application meng-host `ComposeUIViewController` via shared module.
  - `shared`: KMP Library menargetkan `android`, `iosArm64`, dan `iosSimulatorArm64`.
- **Version Catalog**: Menggunakan format standar `libs.versions.toml` di root project.

---

## 3. Tech Stack Final & Version Catalog (`libs.versions.toml`)

```toml
[versions]
# Platform & Core Android
androidx-activity-compose = "1.13.0"
androidx-lifecycle = "2.11.0"

# Kotlin Coroutines, Serialization & DateTime
kotlinx-coroutines = "1.10.1"
kotlinx-serialization = "1.8.0"
kotlinx-datetime = "0.6.2"

# Dependency Injection (Koin)
koin = "4.0.2"

# Networking (Ktor Client 3.x)
ktor = "3.1.1"

# Persistence (Jetpack DataStore Multiplatform)
androidx-datastore = "1.1.3"

# Image Loading (Coil 3 Multiplatform)
coil3 = "3.1.0"

# Navigation (Compose Multiplatform Navigation)
navigation-compose = "2.8.0-alpha10"

# QR Code Rendering Multiplatform
qrose = "1.0.1"

# Testing
turbine = "1.2.0"

[libraries]
# Android & Compose Core
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity-compose" }
androidx-lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle" }
androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }

# Kotlinx Libraries
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

# Koin Multiplatform
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

# Ktor Client Multiplatform
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }

# Local DataStore
datastore-preferences-core = { module = "androidx.datastore:datastore-preferences-core", version.ref = "androidx-datastore" }

# Coil 3 Image Loading
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil3" }
coil-network-ktor3 = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil3" }

# Navigation Compose
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }

# QR Code Generator
qrose = { module = "io.github.alexzhirkevich:qrose", version.ref = "qrose" }

# Testing Libraries
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

---

## 4. Arsitektur & Struktur Folder: Package-by-Layer

Pola arsitektur menggunakan **Package-by-Layer**. Layer tingkat teratas membagi tanggung jawab arsitektural (`core`, `data`, `domain`, `presentation`, `di`), dan di dalamnya dikelompokkan berdasarkan area fitur yang identik (`movie`, `cinema`, `booking`, `ticket`).

### Aturan Batas Fitur & Tempat Kode Bersama
- **Batas Layer**: `presentation` -> `domain` (dan `core`); `data` -> `domain` (dan `core`). `presentation` dilarang mengimpor `data`.
- **Batas Antar Fitur**: Komunikasi lintas fitur pada presentasi hanya lewat rute navigasi. Orkestrasi bisnis multi-fitur (misal booking + seat) dilakukan oleh Use Case komposit di `domain/booking/usecase/ConfirmBookingUseCase.kt`.
- **Kode Bersama (`core/`)**: Komponen yang dipakai oleh >= 2 fitur (seperti format tanggal, mata uang rupiah, `AppResult`, `AppError`, base theme, shimmer) wajib berada di `core/`.

```text
shared/src/
├── org/ariqhikari/movietix/
│   ├── App.kt                          # Root Compose entry point & AppTheme
│   ├── core/                           # Komponen fondasi bersama
│   │   ├── data/
│   │   │   ├── network/                # HttpClientFactory, expect getHttpEngine
│   │   │   └── storage/                # DataStoreFactory, expect createDataStorePath
│   │   ├── domain/
│   │   │   └── model/                  # AppResult, AppError
│   │   └── presentation/
│   │       ├── theme/                  # Colors, Typography, Shapes, MovieTixTheme
│   │       └── components/             # Shimmer, MovieTixButton, ErrorStateView
│   ├── data/                           # LAYER DATA
│   │   ├── movie/
│   │   │   ├── remote/                 # TmdbApiService, MovieResponseDto, MovieDetailDto
│   │   │   ├── mapper/                 # MovieMapper.kt
│   │   │   └── repository/             # MovieRepositoryImpl.kt
│   │   ├── cinema/
│   │   │   ├── local/                  # ProceduralScheduleEngine.kt
│   │   │   └── repository/             # CinemaScheduleRepositoryImpl.kt
│   │   ├── booking/
│   │   │   └── repository/             # BookingRepositoryImpl.kt
│   │   └── ticket/
│   │       ├── local/                  # TicketDataStoreDataSource.kt, TicketEntity.kt
│   │       ├── mapper/                 # TicketMapper.kt
│   │       └── repository/             # TicketRepositoryImpl.kt
│   ├── domain/                         # LAYER DOMAIN
│   │   ├── movie/
│   │   │   ├── model/                  # Movie, MovieDetail, CastMember, TrailerVideo
│   │   │   ├── repository/             # MovieRepository.kt (Interface)
│   │   │   └── usecase/                # GetNowPlayingMoviesUseCase, GetMovieDetailUseCase
│   │   ├── cinema/
│   │   │   ├── model/                  # Cinema, Showtime, Seat, StudioType
│   │   │   ├── repository/             # CinemaScheduleRepository.kt (Interface)
│   │   │   └── usecase/                # GetCinemasWithSchedulesUseCase, GetSeatLayoutUseCase
│   │   ├── booking/
│   │   │   ├── model/                  # BookingOrder, PaymentMethod, PaymentResult
│   │   │   ├── repository/             # BookingRepository.kt (Interface)
│   │   │   └── usecase/                # ConfirmBookingUseCase.kt, ProcessPaymentSimulationUseCase.kt
│   │   └── ticket/
│   │       ├── model/                  # Ticket, TicketStatus
│   │       ├── repository/             # TicketRepository.kt (Interface)
│   │       └── usecase/                # GetTicketHistoryUseCase.kt, SaveTicketUseCase.kt
│   ├── presentation/                   # LAYER PRESENTATION
│   │   ├── movie/
│   │   │   ├── home/                   # HomeScreen.kt, HomeViewModel.kt, HomeContract.kt
│   │   │   └── detail/                 # DetailScreen.kt, DetailViewModel.kt, DetailContract.kt
│   │   ├── cinema/
│   │   │   └── seatmap/                # SeatMapScreen.kt, SeatMapViewModel.kt, SeatMapContract.kt
│   │   ├── booking/
│   │   │   └── checkout/               # CheckoutBottomSheet.kt, CheckoutViewModel.kt, CheckoutContract.kt
│   │   └── ticket/
│   │       ├── history/                # TicketHistoryScreen.kt, TicketHistoryViewModel.kt
│   │       └── detail/                 # TicketDetailScreen.kt, TicketDetailViewModel.kt
│   ├── di/                             # LAYER DEPENDENCY INJECTION
│   │   ├── CoreModule.kt
│   │   ├── MovieModule.kt
│   │   ├── CinemaModule.kt
│   │   ├── BookingModule.kt
│   │   ├── TicketModule.kt
│   │   └── KoinInit.kt
│   └── navigation/                     # AppNavigation.kt, ScreenDestination.kt
```

---

## 5. Desain Modul Koin (Dependency Injection)

Inisialisasi DI dibagi per modul terstruktur:
1. **`coreModule`**:
   - `single { getHttpEngine() }` (platform-specific via expect/actual)
   - `single { createHttpClient(get(), Secrets.TMDB_ACCESS_TOKEN, isDebug = isDebug) }`
   - `single { createDataStore() }`
   - `single { Dispatchers.IO }`
2. **`movieModule`**:
   - `single { TmdbApiService(get()) }`
   - `single<MovieRepository> { MovieRepositoryImpl(get(), get()) }`
   - `factory { GetNowPlayingMoviesUseCase(get()) }`
   - `factory { GetPopularMoviesUseCase(get()) }`
   - `factory { GetUpcomingMoviesUseCase(get()) }`
   - `factory { GetMovieDetailUseCase(get()) }`
   - `viewModel { HomeViewModel(get(), get(), get()) }`
   - `viewModel { (movieId: Int) -> DetailViewModel(movieId, get()) }`
3. **`cinemaModule`**:
   - `single { ProceduralScheduleEngine() }`
   - `single<CinemaScheduleRepository> { CinemaScheduleRepositoryImpl(get(), get()) }`
   - `factory { GetCinemasWithSchedulesUseCase(get()) }`
   - `factory { GetSeatLayoutUseCase(get()) }`
   - `viewModel { (scheduleId: String) -> SeatMapViewModel(scheduleId, get(), get()) }`
4. **`bookingModule`**:
   - `factory { ProcessPaymentSimulationUseCase(get()) }`
   - `factory { ConfirmBookingUseCase(get(), get()) }`
   - `viewModel { CheckoutViewModel(get(), get()) }`
5. **`ticketModule`**:
   - `single { TicketDataStoreDataSource(get()) }`
   - `single<TicketRepository> { TicketRepositoryImpl(get()) }`
   - `factory { GetTicketHistoryUseCase(get()) }`
   - `factory { SaveTicketUseCase(get()) }`
   - `viewModel { TicketHistoryViewModel(get()) }`
   - `viewModel { (bookingCode: String) -> TicketDetailViewModel(bookingCode, get()) }`

---

## 6. Desain Data Layer & Transaksi Atomik

*Rincian lengkap DTO, Ktor Client, dan Skema Serialisasi tersedia di [docs/DATA_LAYER.md](file:///Users/ariqhikari/Projects/MovieTix/docs/DATA_LAYER.md).*

### 6.1 TMDB Endpoints & Keamanan Jaringan
- Base URL: `https://api.themoviedb.org/3/`
- Autentikasi: `Authorization: Bearer <TMDB_ACCESS_TOKEN>` (dibaca via `Secrets.TMDB_ACCESS_TOKEN` dari file `Secrets.kt` yang di-gitignore).
- Header Authorization disanitasi dari log Ktor agar tidak bocor di terminal, dan logging dimatikan pada release build.

### 6.2 Procedural Deterministic Engine
- Seed dibentuk dari: `seed = abs("$movieId-$dateStr-$cinemaId".hashCode())`.
- Menghasilkan bioskop fiktif prestisius (XXI, CGV, Cinepolis), studio (IMAX, Velvet, Regular), harga tiket, dan 25-40% pre-occupied seats yang identik 100% saat dibuka kembali.

### 6.3 Atomisitas Pemesanan & Rollback (`ConfirmBookingUseCase`)
Pemesanan tiket menggabungkan dua operasi penyimpanan lokal secara aman:
1. `cinemaScheduleRepository.reserveSeats(order.scheduleId, order.selectedSeats)`
2. `ticketRepository.saveTicket(ticket)`
3. **Rollback**: Jika penyimpanan tiket gagal, sistem memanggil kompensasi `cinemaScheduleRepository.releaseSeats(order.scheduleId, order.selectedSeats)` sehingga status kursi kembali tersedia dan data tetap konsisten.

---

## 7. Desain Domain Layer

Domain layer adalah *pure Kotlin* yang memuat logika bisnis:
- Model: `Movie`, `MovieDetail`, `CastMember`, `Cinema`, `Showtime`, `Seat`, `Ticket`, `BookingOrder`.
- Kontrak Repository: `MovieRepository`, `CinemaScheduleRepository`, `TicketRepository`.
- Kontrak Error: Sealed interface `AppResult<out T>` dan `AppError` fungsional.

---

## 8. Navigasi & Hirarki Layar

Navigasi menggunakan **Jetpack Navigation Compose Multiplatform** dengan *Type-Safe Routing* berbasis `@Serializable`:

```kotlin
@Serializable sealed interface ScreenDestination {
    @Serializable data object Home : ScreenDestination
    @Serializable data class Detail(val movieId: Int) : ScreenDestination
    @Serializable data class SeatMap(
        val movieId: Int, 
        val cinemaId: String, 
        val scheduleId: String, 
        val date: String
    ) : ScreenDestination
    @Serializable data class TicketDetail(val bookingCode: String) : ScreenDestination
    @Serializable data object TicketHistory : ScreenDestination
}
```

Pola Presentation seragam: Setiap layar diatur oleh `[Feature]Contract` yang memuat `data class State(...)`, `sealed interface Action`, dan `sealed interface Effect`.

---

## 9. Ringkasan Desain Sistem (Design System Summary)

*Rincian lengkap dan wireframe tersedia di [docs/DESIGN_SYSTEM.md](file:///Users/ariqhikari/Projects/MovieTix/docs/DESIGN_SYSTEM.md).*

- **Tema**: *"Midnight Cinema & Ticket Craft"* — Latar gelap obsidian (`#0B0E14`), kartu surface (`#131822`), aksen emas tembaga (`#E5A93C`) dan neon ember (`#F08C4A`).
- **Verifikasi Kontras WCAG AA (Hasil Script)**:
  - `light-primary` diperbarui menjadi **`#825000`** (Amber Ochre Deep) menghasilkan rasio **6.78:1** di atas putih murni, lolos mutlak WCAG AA untuk teks normal.
  - `state-error` diperbarui menjadi **`#FF6B6B`** di Dark Theme menghasilkan rasio **5.87:1** di atas `cinema-surface-card` `#1A202E`, lolos WCAG AA.
- **Tipografi**: Display font **`Outfit`** + Body font **`Plus Jakarta Sans`**.
- **Komponen Spesial**:
  - *Perforated Ticket*: Kartu tiket dengan notch setengah lingkaran pada kedua sisi divider putus-putus.
  - *Multi-Modal Seat Map*: Kursi bioskop dibedakan dengan border stroke, ikon glyph (centang `v` dan silang `X`), dan label kontras.
  - *Haptic Feedback*: Menggunakan `LocalHapticFeedback.current` di `commonMain`.
  - *Wireframe*: Bersih 100% dari emoji, menggunakan representasi ASCII standar (`[*]`, `[v]`, `[X]`, `[o]`).

---

## 10. Git & Commit Convention

Seluruh kontribusi kode dan dokumentasi saat implementasi **wajib** mematuhi standar [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/).

### 10.1 Format Commit Message
```text
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### 10.2 Type yang Diizinkan
- `feat` : Fitur baru untuk pengguna atau domain bisnis.
- `fix` : Perbaikan bug pada logika bisnis, data, atau UI.
- `docs` : Penambahan atau pembaruan dokumentasi/markdown.
- `style` : Perubahan token tema, font, spacing, tanpa merubah logika bisnis.
- `refactor` : Perubahan struktur kode tanpa merubah perilaku fitur.
- `perf` : Optimasi performa render atau eksekusi query.
- `test` : Penambahan atau perbaikan unit test / mock test.
- `build` : Perubahan konfigurasi build tool, Amper, atau Gradle.
- `ci` : Konfigurasi continuous integration / workflow.
- `chore` : Pembersihan kode atau pemeliharaan dependensi berkala.

### 10.3 Scope Resmi Proyek
Scope harus mencerminkan area arsitektur MovieTix:
- `core` : Jaringan dasar, storage path, AppResult, utilitas bersama.
- `movie` : Katalog film TMDB, detail film, cast, dan video.
- `cinema` : Peta kursi, procedural engine, jadwal tayang, dan bioskop.
- `booking` : Alur checkout, pembayaran dummy, `ConfirmBookingUseCase`.
- `ticket` : E-tiket berperforasi, DataStore tiket, QR code.
- `di` : Konfigurasi modul Koin dan inisialisasi injection.
- `design-system`: Token warna, tipografi, tema, dan komponen UI dasar.
- `deps` : Pembaruan katalog dependensi di `libs.versions.toml`.
- `navigation` : NavHost, ScreenDestination, dan transisi layar.

### 10.4 Aturan Disiplin Commit
1. **Huruf Kecil & Imperatif**: Description menggunakan huruf kecil, bentuk kata kerja imperatif (misal `add`, `implement`, `fix`), tanpa titik di akhir, maksimal 72 karakter.
2. **Satu Commit = Satu Perubahan Logis**: Commit dilakukan segera setelah satu task/sub-task di `docs/TASKS.md` selesai dan verifikasi build/test lulus. Dilarang menggabungkan beberapa task berbeda dalam satu commit besar.
3. **Breaking Change**: Ditandai dengan tanda seru `!` setelah type/scope (misal `feat(movie)!: ...`) atau footer `BREAKING CHANGE: <penjelasan>`.
4. **Larangan Kebocoran Rahasia**: Dilarang meng-commit file `local.properties`, file keystore, atau token API TMDB ke git commit history.

### 10.5 Contoh Commit Message Valid Tiap Fase
- **Fase 0**: `build(deps): add koin ktor datastore and coil to version catalog`
- **Fase 0**: `style(design-system): implement color tokens typography and theme`
- **Fase 0**: `feat(core): setup ktor http client and appresult error handling`
- **Fase 1**: `feat(movie): implement tmdb remote datasource and repository`
- **Fase 1**: `feat(movie): build home screen with hero banner and catalog grid`
- **Fase 2**: `feat(movie): add movie detail credits and videos api integration`
- **Fase 3**: `feat(cinema): implement deterministic procedural schedule engine`
- **Fase 3**: `feat(cinema): create interactive seat map screen with haptic feedback`
- **Fase 4**: `feat(booking): build checkout bottom sheet and simulation dialogs`
- **Fase 5**: `feat(booking): implement confirm booking use case with atomic rollback`
- **Fase 5**: `feat(ticket): build perforated ticket detail and history screens`
- **Fase 6**: `test(core): add comprehensive common unit tests and mock repositories`
- **Fase 6**: `docs: finalize portfolio documentation and readme`

---

## 11. Roadmap Bertahap (Roadmap & Phases)

*Checklist implementasi interaktif dapat dilihat di [docs/TASKS.md](file:///Users/ariqhikari/Projects/MovieTix/docs/TASKS.md).*

| Fase | Fokus & Tujuan | Output Utama | Definition of Done (DoD) |
|---|---|---|---|
| **Fase 0** | Fondasi, Koin DI, Ktor, DataStore, Font, dan Tema | `core/`, `theme/`, `di/`, asset font | Build sukses di Android & iOS, Koin terinisialisasi. |
| **Fase 1** | Katalog Film (TMDB API, Carousel, Filter, Pagination) | `HomeScreen`, `MovieRepositoryImpl`, Shimmer | Film nyata tampil dari TMDB, pagination lancar, error retry bekerja. |
| **Fase 2** | Detail Film Sinematik (Backdrop, Cast, Trailer Link) | `DetailScreen`, `CastCarousel`, Video link | Info film lengkap, pemeran tampil rapi, poster hero transisi halus. |
| **Fase 3** | Jadwal Bioskop & Denah Kursi Interaktif | `ProceduralEngine`, `SeatMapScreen`, Selector | Jadwal deterministik muncul, kursi responsif saat di-tap, kalkulasi harga tepat. |
| **Fase 4** | Checkout & Simulasi Pembayaran Dummy | `CheckoutBottomSheet`, State Machine Bayar | Alur pembayaran fiktif berjalan mulus dengan animasi loading & dialog hasil. |
| **Fase 5** | E-Tiket Berperforasi, QR Offline & Transaksi Atomik | `TicketDetailScreen`, `qrose` QR, `ConfirmBooking` | Tiket tersimpan persisten secara atomik, QR render offline tanpa internet. |
| **Fase 6** | Polish, Unit Tests, Robustness & Portfolio README | Unit tests di `commonTest`, documentation | Seluruh unit test lulus, UI responsif dan siap dipresentasikan. |

---

## 12. Strategi Pengujian (Testing Strategy)

1. **Unit Testing di `commonTest`**:
   - Seluruh logika bisnis diuji pada level multiplatform murni tanpa emulator Android atau simulator iOS.
   - Menggunakan **Fake Repository** di `shared/test/fakes/` (`FakeMovieRepository`, `FakeCinemaScheduleRepository`, `FakeTicketRepository`).
2. **ViewModel Testing dengan Turbine**:
   - Menguji transisi `State` dan emisi `Effect` secara deterministik menggunakan `app.cash.turbine:turbine` dan `StandardTestDispatcher`.
3. **Procedural Engine Test**:
   - Menjamin bahwa algoritma hashing deterministik menghasilkan jadwal dan kursi yang 100% identik untuk input yang sama di platform JVM dan Kotlin/Native.
4. **Atomic Booking Rollback Test**:
   - Menguji skenario `ConfirmBookingUseCase` saat `saveTicket` melempar `AppError.Storage`, memastikan `releaseSeats` dipanggil untuk melepaskan kursi yang sempat dipesan.

---

## 13. Risiko Teknis & Mitigasinya

| Risiko Teknis | Potensi Dampak | Strategi Mitigasi MovieTix |
|---|---|---|
| **Perbedaan Perilaku Ktor Engine di iOS** | iOS Darwin engine memiliki aturan SSL strict dan App Transport Security (ATS). | Konfigurasi Darwin Engine secara eksplisit di `src@ios` dengan `configureRequest { setAllowsCellularAccess(true) }` dan hanya menggunakan endpoint HTTPS resmi TMDB. |
| **Path File DataStore di iOS Sandbox** | `NSDocumentDirectory` di iOS dapat berbeda struktur path-nya tergantung simulator/device. | Gunakan fungsi platform resmi `NSFileManager.defaultManager.URLForDirectory` dengan `NSDocumentDirectory` untuk memastikan lokasi file selalu valid dan diizinkan sistem. |
| **Amper Token Secrets Integration** | Amper 0.13 toolchain belum mendukung plugin code generation Gradle kustom seperti BuildKonfig. | Menggunakan file Kotlin `Secrets.kt` di `commonMain` yang didaftarkan ke `.gitignore`, dengan template `Secrets.kt.template` di repository git. Bebas overhead runtime dan zero dependency eksternal. |
| **Rate Limit / Kegagalan API TMDB** | Kuota API habis atau koneksi internet pengguna terputus. | Terapkan fallback caching di memory layer, pesan error ramah pengguna dengan tombol *"Coba Lagi"*, dan offline ticket access via DataStore. |

---

## 14. Open Questions (Pertanyaan Terbuka untuk Diskusi)

Berikut adalah beberapa hal teknis dan produk yang masih terbuka untuk dievaluasi sebelum fase implementasi kode:

1. **AndroidContextProvider (Technical Debt)**:
   - Amper saat ini belum mendukung mekanisme auto context provider multiplatform (misal `androidx.startup`). Context Android diinisialisasi secara eksplisit di `MainActivity.onCreate` melalui `AndroidContextProvider.init(this)`. Dicatat sebagai technical debt untuk dievaluasi ulang bila tooling Amper / Koin berkembang.
2. **Kapasitas Cache Offline Gambar Coil 3**:
   - Mengingat poster film dan foto pemeran diambil dari TMDB, apakah perlu ditetapkan batas kuota disk cache Coil (misal maksimal 100 MB) pada perangkat mobile agar tidak membebani memori lokal pengguna?
3. **Format Identifikasi Booking Code**:
   - Saat ini kode booking dirancang berformat `MTX-XXXXXX` (6 digit numerik acak deterministik). Apakah kode ini perlu menyertakan tanggal pemesanan (misal `MTX-261112-9281`) untuk mempermudah identifikasi manual saat review?
4. **Penanganan Trailer YouTube di iOS**:
   - Pada Android, membuka URL YouTube dapat memicu YouTube App intent secara langsung. Pada iOS, apakah cukup membuka Safari / in-app Safari View Controller via `openURL`, atau direncanakan integrasi WebView inline di masa depan?
