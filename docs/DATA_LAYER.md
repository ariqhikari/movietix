# MovieTix — Data Layer & Persistence Architecture

Dokumen ini menjelaskan secara mendalam layer data pada **MovieTix**, mencakup integrasi remote API (The Movie Database / TMDB), sistem pembangkitan jadwal & peta kursi prosedural (*deterministic seeding engine*), penyimpanan persisten menggunakan Jetpack DataStore Multiplatform, serta desain transaksi atomik pemesanan tiket dengan mekanisme kompensasi (*rollback*).

---

## 1. Remote Data Source: TMDB API v3

MovieTix memanfaatkan TMDB API v3 sebagai sumber data katalog film.

### 1.1 Konfigurasi Dasar Jaringan
- **Base URL Lengkap**: `https://api.themoviedb.org/3/`
- **Image Base URL (Poster)**: `https://image.tmdb.org/t/p/w500`
- **Image Base URL (Backdrop)**: `https://image.tmdb.org/t/p/w1280`
- **Autentikasi Aman**: Menggunakan Header HTTP standar:  
  `Authorization: Bearer <TMDB_READ_ACCESS_TOKEN>`  
  Pendekatan Bearer token dipilih dibandingkan query parameter agar token API tidak terekspos pada URL request, riwayat proxy, maupun log jaringan.
- **Sumber Kunci**: Nilai token dibaca secara statis dari objek `Secrets.TMDB_ACCESS_TOKEN` di `core/data/network/Secrets.kt` (file ter-gitignore). File template aman `Secrets.kt.template` disediakan di repositori git untuk onboarding pengembang baru. Parameter `isDebug` dipropagasikan dari platform entry point (`MainActivity.kt` di Android dan `ViewController.kt` di iOS) ke `HttpClientFactory`, mematikan logging pada release build.

### 1.2 Endpoints TMDB yang Digunakan

| Kebutuhan Fitur | Endpoint URL | Parameter Kunci | DTO Model |
|---|---|---|---|
| **Now Playing Movies** | `GET /movie/now_playing` | `page`, `language=id-ID` (fallback `en-US`), `region=ID` | `MovieResponseDto` |
| **Popular Movies** | `GET /movie/popular` | `page`, `language=id-ID`, `region=ID` | `MovieResponseDto` |
| **Upcoming Movies** | `GET /movie/upcoming` | `page`, `language=id-ID`, `region=ID` | `MovieResponseDto` |
| **Genre List** | `GET /genre/movie/list` | `language=id-ID` | `GenreListResponseDto` |
| **Detail Film Lengkap** | `GET /movie/{movie_id}` | `language=id-ID` | `MovieDetailDto` |
| **Daftar Pemeran & Kru** | `GET /movie/{movie_id}/credits` | `language=en-US` | `CreditsDto` |
| **Video & Trailer Film** | `GET /movie/{movie_id}/videos` | `language=en-US` | `VideosDto` |

---

### 1.3 Spesifikasi Data Transfer Objects (DTO)

Semua DTO ditempatkan pada package `data/movie/remote/dto/` dengan `kotlinx.serialization`:

```kotlin
package org.ariqhikari.movietix.data.movie.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponseDto(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<MovieDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("genre_ids") val genreIds: List<Int>? = null
)

@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("genres") val genres: List<GenreDto>? = null,
    @SerialName("tagline") val tagline: String? = null
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
data class CreditsDto(
    @SerialName("id") val id: Int,
    @SerialName("cast") val cast: List<CastDto>? = null
)

@Serializable
data class CastDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("character") val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("order") val order: Int? = null
)

@Serializable
data class VideosDto(
    @SerialName("id") val id: Int,
    @SerialName("results") val results: List<VideoDto>? = null
)

@Serializable
data class VideoDto(
    @SerialName("id") val id: String,
    @SerialName("key") val key: String, // YouTube Video Key
    @SerialName("name") val name: String,
    @SerialName("site") val site: String, // "YouTube"
    @SerialName("type") val type: String // "Trailer", "Teaser"
)
```

---

### 1.4 Konfigurasi Ktor Client & Keamanan Logging

Factory klien HTTP menjamin integrasi HTTPS yang aman serta pencegahan kebocoran token pada log terminal:

```kotlin
// core/data/network/HttpClientFactory.kt
package org.ariqhikari.movietix.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine, 
    bearerToken: String, 
    isDebug: Boolean = true
): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
                coerceInputValues = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
            // Sensor header otorisasi agar token tidak pernah tercetak di log
            sanitizeHeader { headerName -> headerName.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.themoviedb.org"
                path("3/")
            }
            if (bearerToken.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
        }
    }
}
```

---

## 2. Local Procedural Schedule Engine (Deterministic Seeder)

### 2.1 Konsep Deterministik
`ProceduralScheduleEngine` (berada di `data/cinema/local/`) menghasilkan jadwal tayang dan keterisian kursi secara murni matematis berbasis hashing:
- **Seed Input**: `abs("$movieId-$dateStr-$cinemaId".hashCode())`
- **Jaminan Konsistensi**: Setiap kali user membuka film yang sama pada tanggal dan bioskop yang sama, susunan studio, jam tayang, harga, dan kursi yang terisi (*occupied seats*) **selalu identik 100%**.
- **Variasi Hari**: Menggunakan `DayOfWeek` dari `kotlinx-datetime` (Weekday: Rp 45.000 - Rp 50.000; Weekend: Rp 65.000 - Rp 75.000; Velvet: Rp 120.000).

### 2.2 Template Bioskop & Tata Letak Kursi
- **Bioskop**: Grand Cinema XXI, CGV Starium & Velvet, Cinepolis Max, Flix Cinema.
- **Studio**: Regular 2D, IMAX 2D, Velvet Suite.
- **Denah Kursi**: 8 Baris (A s.d. H) x 10 Kolom (1 s.d. 10) = 80 kursi, dengan lorong jalan (*aisle*) di antara kolom 4 dan 5.
- **Simulasi Penonton Lain**: 25% - 40% kursi ditandai *occupied* oleh generator deterministik saat jadwal dibaca.

---

## 3. Skema DataStore Multiplatform

MovieTix menggunakan **Jetpack DataStore Preferences** (`androidx.datastore:datastore-preferences-core`) yang berjalan di Android dan iOS.

### 3.1 Skema Kunci Penyimpanan
```text
DataStore Preferences Key Namespace:
├── "occupied_seats_<scheduleId>"       ──► JSON: List<String> (Kursi yang dipesan user)
└── "ticket_history_v1"                 ──► JSON: TicketHistoryContainer
```

### 3.2 Entitas Data Lokal (TicketEntity)

Ditempatkan pada `data/ticket/local/`:

```kotlin
package org.ariqhikari.movietix.data.ticket.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketHistoryContainer(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("tickets") val tickets: List<TicketEntity> = emptyList()
)

@Serializable
data class TicketEntity(
    @SerialName("booking_code") val bookingCode: String, // e.g. "MTX-948201"
    @SerialName("movie_id") val movieId: Int,
    @SerialName("movie_title") val movieTitle: String,
    @SerialName("poster_url") val posterUrl: String?,
    @SerialName("backdrop_url") val backdropUrl: String?,
    @SerialName("cinema_name") val cinemaName: String,
    @SerialName("studio_name") val studioName: String,
    @SerialName("studio_type") val studioType: String, // "IMAX", "REGULAR", "VELVET"
    @SerialName("showtime_iso") val showtimeIso: String,
    @SerialName("seats") val seats: List<String>, // e.g. ["C5", "C6"]
    @SerialName("price_per_seat") val pricePerSeat: Long,
    @SerialName("service_fee") val serviceFee: Long,
    @SerialName("total_price") val totalPrice: Long,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("transaction_time_epoch_ms") val transactionTimeEpochMs: Long,
    @SerialName("status") val status: String, // "ACTIVE", "EXPIRED", "CANCELLED"
    @SerialName("qr_payload") val qrPayload: String
)
```

---

## 4. Atomisitas Pemesanan & Strategi Rollback (`ConfirmBookingUseCase`)

### 4.1 Permasalahan Konsistensi
Pemesanan tiket melibatkan dua operasi penyimpanan lokal yang berbeda:
1. Menandai kursi sebagai terisi pada jadwal tertentu (`CinemaScheduleRepository.reserveSeats`).
2. Menyimpan data tiket lunas ke dalam riwayat transaksi (`TicketRepository.saveTicket`).

Bila operasi ke-2 gagal (misal DataStore I/O exception), kursi tidak boleh tetap terpesan dalam kondisi *stuck*, karena pengguna tidak memiliki bukti tiket.

### 4.2 Desain Alur Transaksi & Rollback

```
   [User Konfirmasi Pembayaran]
                │
                ▼
      reserveSeats(order)
      ├── GAGAL ─────────► Return AppResult.Error(SeatAlreadyOccupied / StorageError)
      │
      └── SUKSES
            │
            ▼
       saveTicket(ticket)
       ├── SUKSES ────────► Return AppResult.Success(ticket)
       │
       └── GAGAL
             │
             ▼
        [KOMPENSASI / ROLLBACK]
        releaseSeats(scheduleId, seats)
             │
             ▼
        Return AppResult.Error(SaveTicketFailed)
```

### 4.3 Implementasi Kontrak Domain: `ConfirmBookingUseCase`

```kotlin
// domain/booking/usecase/ConfirmBookingUseCase.kt
package org.ariqhikari.movietix.domain.booking.usecase

import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository

class ConfirmBookingUseCase(
    private val cinemaScheduleRepository: CinemaScheduleRepository,
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(order: BookingOrder): AppResult<Ticket> {
        // Langkah 1: Kunci/reservasi kursi di DataStore bioskop
        val reserveResult = cinemaScheduleRepository.reserveSeats(order.scheduleId, order.selectedSeats)
        if (reserveResult is AppResult.Error) {
            return reserveResult
        }

        // Siapkan objek tiket domain
        val ticket = order.toTicket()

        // Langkah 2: Simpan bukti tiket ke DataStore riwayat transaksi
        val saveTicketResult = ticketRepository.saveTicket(ticket)
        return when (saveTicketResult) {
            is AppResult.Success -> AppResult.Success(ticket)
            is AppResult.Error -> {
                // Langkah 3 (Kompensasi / Rollback): Batalkan reservasi kursi bila tiket gagal disimpan
                cinemaScheduleRepository.releaseSeats(order.scheduleId, order.selectedSeats)
                AppResult.Error(
                    AppError.Booking.SaveTicketFailed("Gagal menyimpan transaksi tiket. Kursi telah dilepaskan kembali.")
                )
            }
        }
    }
}
```

---

## 5. Kontrak Interface Repository

### 5.1 MovieRepository
```kotlin
package org.ariqhikari.movietix.domain.movie.repository

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo

interface MovieRepository {
    suspend fun getNowPlayingMovies(page: Int): AppResult<List<Movie>>
    suspend fun getPopularMovies(page: Int): AppResult<List<Movie>>
    suspend fun getUpcomingMovies(page: Int): AppResult<List<Movie>>
    suspend fun getMovieDetail(movieId: Int): AppResult<MovieDetail>
    suspend fun getMovieCredits(movieId: Int): AppResult<List<CastMember>>
    suspend fun getMovieTrailers(movieId: Int): AppResult<List<TrailerVideo>>
}
```

### 5.2 CinemaScheduleRepository
```kotlin
package org.ariqhikari.movietix.domain.cinema.repository

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout

interface CinemaScheduleRepository {
    suspend fun getCinemasWithSchedules(movieId: Int, date: String): AppResult<List<Cinema>>
    suspend fun getSeatLayout(scheduleId: String, movieId: Int, date: String): AppResult<SeatLayout>
    suspend fun reserveSeats(scheduleId: String, selectedSeatIds: List<String>): AppResult<Unit>
    suspend fun releaseSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit>
}
```

### 5.3 TicketRepository
```kotlin
package org.ariqhikari.movietix.domain.ticket.repository

import kotlinx.coroutines.flow.Flow
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.model.Ticket

interface TicketRepository {
    fun getTickets(): Flow<List<Ticket>>
    suspend fun getTicketByBookingCode(bookingCode: String): AppResult<Ticket>
    suspend fun saveTicket(ticket: Ticket): AppResult<Unit>
}
```
