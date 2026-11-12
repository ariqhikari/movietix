# MovieTix — Architectural Blueprint & Engineering Design

Dokumen ini mendefinisikan standar arsitektur perangkat lunak untuk aplikasi **MovieTix** (Kotlin Multiplatform: Android & iOS). Tujuannya adalah memastikan kode memiliki modularitas tinggi, *separation of concerns* yang ketat, kemudahan pengujian (*testability*), dan keterbacaan tingkat profesional.

---

## 1. Prinsip & Filosofi Arsitektur

MovieTix mengadopsi **Pragmatic Clean Architecture** yang dikombinasikan dengan pola **Unidirectional Data Flow (UDF / MVI-lite)** pada layer presentasi.

```
┌──────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│  Compose Multiplatform UI  ◄───►  Screen ViewModel (UDF) │
└─────────────────────────────┬────────────────────────────┘
                              │ Calls Use Cases
                              ▼
┌──────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                        │
│   Use Cases (Interactors)  ◄───►  Domain Models          │
│   Repository Interfaces    ◄───►  AppResult & AppError   │
└─────────────────────────────▲────────────────────────────┘
                              │ Implemented by
┌─────────────────────────────┴────────────────────────────┐
│                       DATA LAYER                         │
│   Repository Implementations                             │
│   ├── Remote Data Source (Ktor Client + TMDB DTOs)       │
│   ├── Local Data Source  (Jetpack DataStore + Entities)  │
│   └── Mappers (DTO ──► Domain, Entity ◄──► Domain)       │
└──────────────────────────────────────────────────────────┘
```

### 1.1 Dependency Inversion Principle (DIP) & Boundary Rules
1. **Domain Layer**: Inti aplikasi murni (*pure Kotlin*), tidak memiliki dependensi terhadap framework Android, iOS, Ktor, DataStore, maupun Compose.
2. **Data Layer**: Mengimplementasikan interface repository yang dideklarasikan di Domain. Bergantung pada Ktor, DataStore, Serialization, dan Domain.
3. **Presentation Layer**: Bergantung pada Domain (dan Core), tidak pernah mengimpor atau memanggil Data layer secara langsung.
4. **DI Layer**: Modul Koin yang menghubungkan implementasi Data, Use Case Domain, dan ViewModel Presentation.
5. Seluruh komunikasi antar komponen menggunakan *Constructor Injection* via Koin dan antarmuka abstraksi.

---

## 2. Struktur Modul & Folder: Package-by-Layer

### 2.1 Keputusan Strategi Folder
Sesuai standar arsitektur terstruktur, MovieTix menggunakan **Package-by-Layer**. Layer tingkat teratas terdiri dari:
- `core/` : Komponen fondasi bersama (network engine, storage path, base theme, components, AppResult).
- `data/` : Implementasi repository, remote DTO, local DataStore, dan mappers.
- `domain/` : Model bisnis murni, interface repository, dan use cases.
- `presentation/` : UI Compose Multiplatform, ViewModel, dan kontrak UDF.
- `di/` : Modul dependency injection Koin.

Di dalam layer `data`, `domain`, dan `presentation`, kode dikelompokkan berdasarkan area fitur yang identik:
- `movie` : Fitur katalog, pencarian, dan detail film TMDB.
- `cinema` : Fitur bioskop, studio, jam tayang, dan peta kursi interaktif.
- `booking` : Fitur ringkasan pesanan, alur pembayaran dummy, dan konfirmasi atomik.
- `ticket` : Fitur e-tiket berperforasi, penyimpanan DataStore, dan QR code offline.

### 2.2 Aturan Menjaga Batas Antar Fitur (Feature Boundary Rules)
1. **Isolasi Presentation Antar Fitur**: Layar pada `presentation/movie` dilarang memanggil ViewModel atau state internal dari `presentation/cinema`. Komunikasi antar layar hanya terjadi melalui parameter rute navigasi bertipe aman (`ScreenDestination`).
2. **Orkestrasi Bisnis Lintas Fitur di Domain Layer**: Bila suatu aksi melibatkan lebih dari satu domain (misalnya saat pemesanan tiket melibatkan pemesanan kursi di `cinema` dan penyimpanan bukti di `ticket`), orkestrasi tersebut **wajib** dilakukan di dalam Use Case khusus pada domain (`ConfirmBookingUseCase`), bukan di Presentation atau Data layer.
3. **Tempat Kode Bersama (`core/`)**:
   - Utilitas yang digunakan oleh >= 2 fitur (seperti format tanggal, mata uang rupiah, sealed class `AppResult`, base shimmer, dan theme tokens) wajib ditempatkan di `core/`.
   - Kode yang hanya relevan untuk satu fitur dilarang diletakkan di `core/`.

### 2.3 Struktur Direktori `shared/src` (Common Code)

```text
shared/src/
├── org/ariqhikari/movietix/
│   ├── App.kt                          # Root Compose entry point & AppTheme
│   ├── core/                           # Komponen fondasi bersama & utilitas
│   │   ├── data/
│   │   │   ├── network/                # HttpClientFactory, Secrets.kt.template (Secrets.kt gitignored), Ktor engine expect/actual
│   │   │   └── storage/                # DataStoreFactory, storage path expect/actual, AndroidContextProvider
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
│   │   │   ├── local/                  # ProceduralScheduleEngine.kt (Deterministic Seeder)
│   │   │   └── repository/             # CinemaScheduleRepositoryImpl.kt
│   │   ├── booking/
│   │   │   └── repository/             # BookingRepositoryImpl.kt
│   │   └── ticket/
│   │       ├── local/                  # TicketDataStoreDataSource.kt, TicketEntity.kt
│   │       ├── mapper/                 # TicketMapper.kt
│   │       └── repository/             # TicketRepositoryImpl.kt
│   ├── domain/                         # LAYER DOMAIN
│   │   ├── movie/
│   │   │   ├── model/                  # Movie.kt, MovieDetail.kt, CastMember.kt, TrailerVideo.kt
│   │   │   ├── repository/             # MovieRepository.kt (Interface)
│   │   │   └── usecase/                # GetNowPlayingMoviesUseCase, GetMovieDetailUseCase
│   │   ├── cinema/
│   │   │   ├── model/                  # Cinema.kt, Showtime.kt, Seat.kt, StudioType.kt
│   │   │   ├── repository/             # CinemaScheduleRepository.kt (Interface)
│   │   │   └── usecase/                # GetCinemasWithSchedulesUseCase, GetSeatLayoutUseCase
│   │   ├── booking/
│   │   │   ├── model/                  # BookingOrder.kt, PaymentMethod.kt, PaymentResult.kt
│   │   │   ├── repository/             # BookingRepository.kt (Interface)
│   │   │   └── usecase/                # ConfirmBookingUseCase.kt, ProcessPaymentSimulationUseCase.kt
│   │   └── ticket/
│   │       ├── model/                  # Ticket.kt, TicketStatus.kt
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
│   │       ├── history/                # TicketHistoryScreen.kt, TicketHistoryViewModel.kt, TicketHistoryContract.kt
│   │       └── detail/                 # TicketDetailScreen.kt, TicketDetailViewModel.kt, TicketDetailContract.kt
│   ├── di/                             # LAYER DEPENDENCY INJECTION
│   │   ├── CoreModule.kt               # HTTP client, DataStore, Dispatchers
│   │   ├── MovieModule.kt              # Movie data, domain, viewmodels
│   │   ├── CinemaModule.kt             # Cinema data, domain, viewmodels
│   │   ├── BookingModule.kt            # Booking usecases, viewmodels
│   │   ├── TicketModule.kt             # Ticket storage, usecases, viewmodels
│   │   └── KoinInit.kt                 # Inisialisasi Koin multiplatform
│   └── navigation/                     # AppNavigation.kt, ScreenDestination.kt
```

---

## 3. Data Transfer Objects (DTO) vs Domain Model vs Local Entity

Isolasi data antar layer dijaga melalui pemisahan tipe dan mapper biekstensi:

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    MovieDto     │       │      Movie      │       │  TicketEntity   │
│  (TMDB Network) │       │ (Domain Model)  │       │ (DataStore JSON)│
└────────┬────────┘       └────────▲────────┘       └────────┬────────┘
         │                         │                         │
         └──► toDomain(): Movie ───┴───◄ toDomain(): Movie ──┘
```

1. **Remote DTO (`data/movie/remote/*Dto.kt`)**: Merefleksikan payload JSON TMDB API dengan anotasi `@Serializable` dan `@SerialName("snake_case")`. Menoleransi nullability remote data.
2. **Domain Model (`domain/*/model/*.kt`)**: Model bisnis murni, non-null dengan nilai fallback yang masuk akal, bebas anotasi framework.
3. **Local Entity (`data/ticket/local/*Entity.kt`)**: Format persisten DataStore dengan versioning skema (`schema_version`).

---

## 4. Pola Error Handling: `AppResult` & `AppError`

MovieTix menolak pelemparan exception (`throw Exception`) di boundary bisnis. Sebagai gantinya, digunakan sealed interface `AppResult<out T>` fungsional:

```kotlin
// core/domain/model/AppResult.kt
package org.ariqhikari.movietix.core.domain.model

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    sealed interface Network : AppError {
        data object NoInternet : Network
        data object Timeout : Network
        data class ServerError(val code: Int, val message: String?) : Network
        data object Unauthorized : Network // Invalid TMDB API Token
    }
    sealed interface Storage : AppError {
        data class ReadFailed(val cause: String) : Storage
        data class WriteFailed(val cause: String) : Storage
    }
    sealed interface Booking : AppError {
        data object SeatAlreadyOccupied : Booking
        data object PaymentFailedSimulated : Booking
        data class SaveTicketFailed(val cause: String) : Booking
        data object InvalidSelection : Booking
    }
    data class Unknown(val throwable: Throwable) : AppError
}
```

---

## 5. Pola Presentation Layer: Unidirectional Data Flow (UDF)

Setiap layar memiliki kontrak terstandarisasi seragam:

```kotlin
// presentation/movie/home/HomeContract.kt
package org.ariqhikari.movietix.presentation.movie.home

import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieCategory

interface HomeContract {
    data class State(
        val isLoading: Boolean = false,
        val nowPlayingMovies: List<Movie> = emptyList(),
        val popularMovies: List<Movie> = emptyList(),
        val upcomingMovies: List<Movie> = emptyList(),
        val selectedCategory: MovieCategory = MovieCategory.NOW_PLAYING,
        val errorMessage: String? = null
    )

    sealed interface Action {
        data class SelectCategory(val category: MovieCategory) : Action
        data class ClickMovie(val movieId: Int) : Action
        data object RetryFetch : Action
        data object Refresh : Action
    }

    sealed interface Effect {
        data class NavigateToDetail(val movieId: Int) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

---

## 6. Pola `expect / actual` (Prinsip Pembatasan Ketat)

Penggunaan `expect / actual` dibatasi **hanya** pada dua komponen di mana Kotlin Multiplatform tidak memiliki abstraksi standar:

| Kasus Penggunaan | Deklarasi `expect` | `actual` Android | `actual` iOS | Alasan |
|---|---|---|---|---|
| **1. Ktor HTTP Engine** | `expect fun getHttpEngine(): HttpClientEngine` | `OkHttp.create()` | `Darwin.create { configureRequest { setAllowsCellularAccess(true) } }` | Integrasi networking native OS & socket pool. |
| **2. DataStore File Path** | `expect fun createDataStorePath(fileName: String): String` | `context.filesDir.resolve(fileName).absolutePath` | `NSFileManager.defaultManager.URLForDirectory(...).path + "/$fileName"` | Android memerlukan `Context`, iOS memerlukan `NSDocumentDirectory`. |

### Komponen yang DILARANG Menggunakan `expect / actual`:
- **API Key / Token**: Dilarang menggunakan expect/actual maupun runtime file reader. Nilai token TMDB diakses via `Secrets.TMDB_ACCESS_TOKEN` di `commonMain` yang di-gitignore, dengan template aman `Secrets.kt.template`.
- **Haptic Feedback**: Menggunakan `LocalHapticFeedback.current` bawaan Compose Multiplatform di `commonMain`.
- **Fitur Eksternal Platform**: Fitur seperti *Simpan Gambar Tiket*, *Tambah ke Kalender*, dan *Bagikan* dikeluarkan dari scope (ditetapkan sebagai *Future Work*) agar tidak menambah kompleksitas platform-specific.

---

## 7. Keamanan Jaringan & Pengelolaan API Key

1. **HTTPS Base URL Lengkap**: Base URL TMDB dikonfigurasi lengkap dengan path versi:  
   `https://api.themoviedb.org/3/`
2. **Pencegahan Kebocoran API Key pada Log**:
   - Autentikasi utama menggunakan header `Authorization: Bearer <TMDB_ACCESS_TOKEN>`.
   - Logging Ktor disanitasi: header `Authorization` disensor (`sanitizeHeader { it == HttpHeaders.Authorization }`), dan level logging dikendalikan parameter `isDebug: Boolean` yang dipropagasikan dari platform entry point (`MainActivity.kt` di Android via `FLAG_DEBUGGABLE`, dan `ViewController.kt` di iOS via `Platform.isDebugBinary`). Pada release build, logging otomatis menjadi `LogLevel.NONE`.
3. **Sumber Konfigurasi Aman**:
   - Amper 0.13 belum mendukung plugin build Gradle seperti `BuildKonfig` maupun task kustom untuk code generation multiplatform.
   - Oleh karena itu, rahasia dikelola melalui file Kotlin `Secrets.kt` di `shared/src/org/ariqhikari/movietix/core/data/network/Secrets.kt` yang didaftarkan ke `.gitignore`.
   - File template `Secrets.kt.template` disediakan di repository git. Pengembang menyalin template ini ke `Secrets.kt` dan mengisi token Bearer TMDB mereka.

---

## 8. Atomisitas Pemesanan (Transaction & Rollback)

Untuk mencegah ketidakkonsistenan data (kursi terpesan tetapi tiket gagal disimpan), proses checkout diatur oleh Use Case komposit:
- `ConfirmBookingUseCase` menjalankan reservasi kursi terlebih dahulu, disusul penyimpanan tiket.
- Jika penyimpanan tiket gagal, Use Case otomatis mengeksekusi kompensasi rollback (`releaseSeats`) untuk membatalkan reservasi kursi. Detail lengkap didokumentasikan di [docs/DATA_LAYER.md](file:///Users/ariqhikari/Projects/MovieTix/docs/DATA_LAYER.md).

---

## 9. Catatan Arsitektur & Technical Debt

- **AndroidContextProvider (Technical Debt)**:  
  Karena Amper belum mendukung library context initialization otomatis (seperti `androidx.startup`) pada modul multiplatform, `Context` Android diinisialisasi secara eksplisit melalui `AndroidContextProvider.init(this)` di `MainActivity.onCreate` sebelum memanggil `startMovieTixKoin()`. Pendekatan ini dicatat sebagai technical debt untuk dievaluasi ulang ketika Amper atau Koin menyediakan context provider otomatis multiplatform.
- **Koin Multiplatform Context**:  
  Inisialisasi Koin menggunakan `KoinPlatformTools.defaultContext()` di `KoinInit.kt` untuk memastikan kompatibilitas penuh antara Android JVM dan Kotlin/Native (iOS).
