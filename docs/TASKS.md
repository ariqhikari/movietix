# MovieTix — Project Tasks & Implementation Checklist

Dokumen ini berisi daftar tugas (*actionable tasks*) bertahap dari Fase 0 hingga Fase 6 yang dapat dicentang (`[x]`) selama pengerjaan implementasi proyek MovieTix. Setiap task atau fase menyertakan saran pesan commit sesuai standar **Conventional Commits v1.0.0**.

---

## Panduan Commit Task
- **Format**: `<type>(<scope>): <description>` (huruf kecil, imperatif, tanpa titik akhir, maksimal 72 karakter).
- **Scope Resmi**: `core`, `movie`, `cinema`, `booking`, `ticket`, `di`, `design-system`, `deps`, `navigation`.
- **Aturan**: Satu commit dilakukan setelah satu task/sub-task selesai dan build/test lulus.

---

## Fase 0: Fondasi Proyek, Dependensi, DI, Networking & Desain Sistem

**Tujuan:**  
Menyiapkan *version catalog* lengkap, mengonfigurasi Koin DI, Ktor Client HTTPS dengan header Bearer token, DataStore factory, bundel font, dan kerangka desain tema di `commonMain`.

### Checklist Tugas:
- [x] **0.1 Setup Version Catalog & Modul Build**
  - [x] Tambahkan dependensi Koin, Ktor, DataStore, Coil 3, DateTime, Serialization, dan QR Code ke `libs.versions.toml`.
  - [x] Perbarui `shared/module.yaml`, `androidApp/module.yaml`, dan `iosApp/module.yaml` agar mengenali dependensi baru.
  - [x] Konfigurasi `local.properties.template` dan mekanisme token `tmdbAccessToken()` expect/actual.
  - *Commit*: `build(deps): add koin ktor datastore and coil to version catalog`
- [x] **0.2 Desain Sistem & Resource Asset**
  - [x] Unduh font `Outfit` dan `Plus Jakarta Sans` ke `shared/composeResources/font/`.
  - [x] Implementasikan palet warna `MovieTixColors` (`light-primary` `#825000`, `state-error` `#FF6B6B`) dan `MovieTixTypography` di `core/presentation/theme/`.
  - [x] Buat kerangka `MovieTixTheme` yang mendukung status bar edge-to-edge.
  - *Commit*: `style(design-system): implement color tokens typography and theme`
- [x] **0.3 Ktor Networking & Error Handling Core**
  - [x] Deklarasikan `expect fun getHttpEngine(): HttpClientEngine` di `core/data/network/`.
  - [x] Implementasikan `actual` di `src@android` (OkHttp) dan `src@ios` (Darwin).
  - [x] Buat `HttpClientFactory` dengan base URL `https://api.themoviedb.org/3/`, header Bearer token, dan sanitasi logging.
  - [x] Definisikan sealed class `AppResult<T>` dan hirarki `AppError` di `core/domain/model/`.
  - *Commit*: `feat(core): setup ktor http client and appresult error handling`
- [x] **0.4 Local DataStore Storage Core**
  - [x] Deklarasikan `expect fun createDataStorePath(fileName: String): String` di `core/data/storage/`.
  - [x] Implementasikan `actual` untuk Android (`context.filesDir`) dan iOS (`NSDocumentDirectory`).
  - [x] Buat factory instansiasi `DataStore<Preferences>` multiplatform.
  - *Commit*: `feat(core): configure multiplatform datastore factory`
- [ ] **0.5 Dependency Injection (Koin)**
  - [ ] Buat `CoreModule.kt` (HTTP client, DataStore, Dispatchers).
  - [ ] Buat fungsi inisialisasi Koin `initKoin()` yang dipanggil dari `MainActivity` dan `ViewController`.
  - *Commit*: `feat(di): setup core koin module and entry points`

> **Definition of Done (DoD) Fase 0:**  
> Proyek dapat di-build dan dijalankan di Android & iOS simulator tanpa error, font kustom ter-render dengan benar, dan inisialisasi Koin berjalan sukses saat startup.

---

## Fase 1: Katalog & Eksplorasi Film (Movie Discovery)

**Tujuan:**  
Mengimplementasikan integrasi TMDB API untuk memuat film *Now Playing*, *Popular*, dan *Upcoming* dengan *infinite scroll pagination*, carousel hero, serta penanganan state (loading shimmer, error retry, dan empty).

### Checklist Tugas:
- [ ] **1.1 Data Layer Fitur Film**
  - [ ] Definisikan DTO `MovieResponseDto`, `MovieDto`, `GenreDto` di `data/movie/remote/dto/`.
  - [ ] Buat `TmdbApiService` untuk endpoint `/movie/now_playing`, `/movie/popular`, `/movie/upcoming`.
  - [ ] Implementasikan `MovieMapper` (`MovieDto.toDomain(): Movie`) di `data/movie/mapper/`.
  - [ ] Buat `MovieRepositoryImpl` di `data/movie/repository/`.
  - *Commit*: `feat(movie): implement tmdb remote datasource and repository`
- [ ] **1.2 Domain Layer Fitur Film**
  - [ ] Definisikan domain model `Movie`, `MovieCategory`, `Genre` di `domain/movie/model/`.
  - [ ] Definisikan kontrak interface `MovieRepository` di `domain/movie/repository/`.
  - [ ] Buat use cases: `GetNowPlayingMoviesUseCase`, `GetPopularMoviesUseCase`, `GetUpcomingMoviesUseCase`.
  - *Commit*: `feat(movie): create domain models and catalog use cases`
- [ ] **1.3 Presentation Layer Layar Home**
  - [ ] Rancang kontrak `HomeContract` (`State`, `Action`, `Effect`) di `presentation/movie/home/`.
  - [ ] Implementasikan `HomeViewModel` dengan coroutine Flow dan caching kategori.
  - [ ] Buat komponen UI `HeroBannerCarousel` (auto-scroll + page indicator).
  - [ ] Buat komponen UI `MovieCard` (aspect ratio 2:3, corner radius 16dp, rating badge emas, poster Coil 3).
  - [ ] Buat komponen UI `CategoryFilterChips` (Now Playing, Popular, Upcoming).
  - [ ] Buat komponen UI `ShimmerSkeletonList` dan `ErrorStateView`.
  - *Commit*: `feat(movie): build home screen with hero banner and catalog grid`
- [ ] **1.4 Registrasi Koin & Navigasi Home**
  - [ ] Daftarkan `MovieModule.kt` ke Koin.
  - [ ] Setup NavHost dasar untuk rute `ScreenDestination.Home`.
  - *Commit*: `feat(di): register movie module and navigation destination`

> **Definition of Done (DoD) Fase 1:**  
> Layar Home menampilkan data film nyata dari TMDB, poster ter-load sempurna via Coil, filter kategori responsif, skeleton loading muncul saat fetch, dan error state bekerja saat offline.

---

## Fase 2: Detail Film Sinematik (Movie Detail)

**Tujuan:**  
Menampilkan informasi mendalam mengenai film yang dipilih: backdrop hero resolusi tinggi, sinopsis lengkap, daftar genre, durasi, rating ulasan, carousel foto pemeran (*cast*), dan tautan trailer video resmi.

### Checklist Tugas:
- [ ] **2.1 Data & Domain Layer Detail Film**
  - [ ] Tambahkan endpoint TMDB `/movie/{id}`, `/movie/{id}/credits`, dan `/movie/{id}/videos` ke `TmdbApiService`.
  - [ ] Definisikan DTO `MovieDetailDto`, `CreditsDto`, `CastDto`, `VideosDto`.
  - [ ] Implementasikan mapper domain model `MovieDetail`, `CastMember`, `TrailerVideo`.
  - [ ] Perluas `MovieRepositoryImpl` dan buat `GetMovieDetailUseCase`.
  - *Commit*: `feat(movie): add movie detail credits and videos api integration`
- [ ] **2.2 Presentation Layer Detail Film**
  - [ ] Rancang kontrak `DetailContract` (`State`, `Action`, `Effect`) di `presentation/movie/detail/`.
  - [ ] Implementasikan `DetailViewModel` yang menerima parameter `movieId`.
  - [ ] Buat komponen `BackdropHeroHeader` dengan efek *scrim gradient* gelap ke transparan.
  - [ ] Buat layout *overlapping poster* (radius 16dp) dengan info judul, durasi (`Xj Ym`), dan rating.
  - [ ] Buat komponen expandable `SynopsisSection` ("Baca Selengkapnya").
  - [ ] Buat komponen horizontal `CastCarousel` (avatar cast + nama karakter).
  - [ ] Buat komponen `TrailerCard` yang memicu intent/URI trailer resmi.
  - [ ] Buat floating *Sticky Bottom CTA Bar* ("PILIH JADWAL & KURSI").
  - *Commit*: `feat(movie): implement cinematic movie detail screen`

> **Definition of Done (DoD) Fase 2:**  
> Pengguna dapat membuka detail film dari Home, seluruh metadata dan daftar pemeran tampil rapi tanpa lag, serta tombol CTA mengarahkan ke layar jadwal bioskop.

---

## Fase 3: Pemesanan Jadwal & Denah Kursi Interaktif (Schedule & Seat Map)

**Tujuan:**  
Membangun *procedural engine* deterministik untuk jadwal bioskop, pemilih tanggal horizontal, daftar bioskop & jam tayang, serta peta kursi interaktif dengan status *Available*, *Selected*, dan *Occupied*.

### Checklist Tugas:
- [ ] **3.1 Procedural Schedule Engine**
  - [ ] Buat kelas `ProceduralScheduleEngine` di `data/cinema/local/`.
  - [ ] Implementasikan algoritma hash `seed = abs("$movieId-$dateStr-$cinemaId".hashCode())`.
  - [ ] Hasilkan daftar bioskop (XXI, CGV, Cinepolis), tipe studio (IMAX, Velvet, Regular), jam tayang, dan harga tiket.
  - [ ] Hasilkan pre-occupied seats (kursi yang telah diisi penonton lain) secara deterministik.
  - *Commit*: `feat(cinema): implement deterministic procedural schedule engine`
- [ ] **3.2 Repository Jadwal & Integrasi DataStore**
  - [ ] Definisikan kontrak `CinemaScheduleRepository` di `domain/cinema/repository/`.
  - [ ] Buat `CinemaScheduleRepositoryImpl` di `data/cinema/repository/` yang menggabungkan kursi hasil seed + kursi terpesan user dari DataStore.
  - [ ] Buat use cases: `GetCinemasWithSchedulesUseCase`, `GetSeatLayoutUseCase`.
  - *Commit*: `feat(cinema): implement cinema schedule repository and seat layout usecase`
- [ ] **3.3 Presentation Layer Jadwal & Seat Map**
  - [ ] Rancang kontrak `SeatMapContract` (`State`, `Action`, `Effect`) di `presentation/cinema/seatmap/`.
  - [ ] Buat komponen `HorizontalDatePicker` (format nama hari + tanggal angka).
  - [ ] Buat komponen `CinemaShowtimeCard` (nama bioskop, badge studio, slot jam tayang).
  - [ ] Buat komponen `CinemaScreenCurved` (layar lengkung cekung dengan pendar).
  - [ ] Buat komponen `SeatMapGrid`:
    - [ ] Render 8 baris (A-H) x 10 kolom dengan pemisah lorong (*aisle*).
    - [ ] Terapkan visual multi-modal (tersedia: border abu; terpilih: emas + centang `v`; terisi: gelap + silang `X`).
    - [ ] Tambahkan animasi spring bounce dan haptic feedback via `LocalHapticFeedback.current`.
  - [ ] Buat `SeatMapLegend` dan floating checkout bar kalkulasi harga total.
  - *Commit*: `feat(cinema): create interactive seat map screen with haptic feedback`

> **Definition of Done (DoD) Fase 3:**  
> Pengguna dapat memilih tanggal, bioskop, jam tayang, memilih/membatalkan kursi interaktif dengan umpan balik visual instan, dan total harga terkalkulasi secara akurat.

---

## Fase 4: Checkout & Simulasi Pembayaran (Dummy Payment)

**Tujuan:**  
Mengimplementasikan alur konfirmasi pesanan dan simulasi transaksi pembayaran tanpa gateway asli, lengkap dengan pemilihan metode pembayaran fiktif, animasi loading proses, dan penanganan status berhasil/gagal.

### Checklist Tugas:
- [ ] **4.1 Domain & State Machine Pembayaran**
  - [ ] Definisikan enum `PaymentMethod` (BCA Virtual Account, GoPay, Kartu Debit/Kredit).
  - [ ] Definisikan domain model `BookingOrder` dan `PaymentResult` di `domain/booking/model/`.
  - [ ] Buat `ProcessPaymentSimulationUseCase` dengan delay simulasi coroutine dan probabilitas hasil.
  - *Commit*: `feat(booking): implement payment simulation domain models and use case`
- [ ] **4.2 Presentation Layer Checkout Bottom Sheet**
  - [ ] Rancang kontrak `CheckoutContract` (`State`, `Action`, `Effect`) di `presentation/booking/checkout/`.
  - [ ] Buat komponen modal `CheckoutBottomSheet`:
    - [ ] Ringkasan tiket (Judul film, studio, jadwal, daftar kursi).
    - [ ] Rincian biaya (Harga kursi x n + Biaya Layanan Aplikasi).
    - [ ] Radio list pilihan metode pembayaran dummy.
    - [ ] Tombol CTA bayar dengan spinner loading pengganti teks.
  - [ ] Rancang dialog animasi hasil transaksi (Success dialog perayaan dan Failure dialog coba lagi).
  - *Commit*: `feat(booking): build checkout bottom sheet and simulation dialogs`

> **Definition of Done (DoD) Fase 4:**  
> Alur checkout berjalan mulus dari sheet konfirmasi, simulasi loading pembayaran, hingga memicu penyimpanan tiket saat transaksi dinyatakan berhasil.

---

## Fase 5: E-Tiket Berperforasi, QR Code Offline & Transaksi Atomik

**Tujuan:**  
Menyimpan tiket yang telah dibayar ke DataStore persisten secara atomik melalui `ConfirmBookingUseCase`, menampilkan bukti tiket dengan desain fisik berperforasi (*punched notch*), dan me-render QR Code offline dengan `qrose`.

### Checklist Tugas:
- [ ] **5.1 Transaksi Atomik Pemesanan & Rollback**
  - [ ] Implementasikan `ConfirmBookingUseCase` di `domain/booking/usecase/`:
    - [ ] Langkah 1: `reserveSeats(...)`.
    - [ ] Langkah 2: `saveTicket(...)`.
    - [ ] Rollback: Panggil `releaseSeats(...)` bila langkah 2 gagal.
  - *Commit*: `feat(booking): implement confirm booking use case with atomic rollback`
- [ ] **5.2 Integrasi Library QR Code Multiplatform**
  - [ ] Integrasikan `io.github.alexzhirkevich:qrose` untuk rendering QR Code murni di Compose Canvas.
  - *Commit*: `feat(ticket): integrate qrose library for offline qr rendering`
- [ ] **5.3 Data Layer & Persistence Tiket**
  - [ ] Buat `TicketDataStoreDataSource` dan `TicketEntity` di `data/ticket/local/`.
  - [ ] Implementasikan `TicketRepositoryImpl` yang mengekspos `Flow<List<Ticket>>`.
  - [ ] Buat mapper antara `TicketEntity` dan domain `Ticket`.
  - *Commit*: `feat(ticket): implement ticket datastore storage and repository`
- [ ] **5.4 Presentation Layer E-Tiket & Riwayat Transaksi**
  - [ ] Rancang kontrak `TicketDetailContract` dan `TicketHistoryContract` di `presentation/ticket/`.
  - [ ] Buat custom shape Compose `PerforatedTicketCard`:
    - [ ] Efek notch setengah lingkaran pada kedua sisi kartu.
    - [ ] Garis perforasi putus-putus (`- - -`).
    - [ ] Area QR Code kontras tinggi (hitam di atas putih `#FFFFFF`).
  - [ ] Buat layar `TicketDetailScreen` (dengan status tiket aktif dan catatan bahwa fitur share/kalender adalah future work).
  - [ ] Buat layar `TicketHistoryScreen` (daftar kartu tiket lampau dan empty state).
  - *Commit*: `feat(ticket): build perforated ticket detail and history screens`

> **Definition of Done (DoD) Fase 5:**  
> Tiket yang dibeli langsung tersimpan ke DataStore secara atomik, muncul di riwayat tiket, dan QR Code ter-render sempurna serta tetap dapat dilihat meski koneksi internet diputus.

---

## Fase 6: Polish, Pengujian, Robustness & Portofolio Ready

**Tujuan:**  
Melakukan finalisasi kualitas kode, menambahkan unit test di `commonTest`, memastikan kepatuhan WCAG AA, dan menyusun dokumentasi portofolio tingkat profesional.

### Checklist Tugas:
- [ ] **6.1 Unit Testing di `commonTest`**
  - [ ] Buat `FakeMovieRepository`, `FakeCinemaScheduleRepository`, dan `FakeTicketRepository`.
  - [ ] Unit test `GetNowPlayingMoviesUseCaseTest` (verifikasi pemrosesan dan mapping).
  - [ ] Unit test `ProceduralScheduleEngineTest` (pastikan determinisme hashing 100% konsisten).
  - [ ] Unit test `ConfirmBookingUseCaseTest` (uji skenario sukses dan skenario rollback saat `saveTicket` gagal).
  - [ ] Unit test `HomeViewModelTest` menggunakan `Turbine` untuk mengetes aliran `State` dan `Effect`.
  - *Commit*: `test(core): add comprehensive common unit tests and mock repositories`
- [ ] **6.2 Aksesibilitas & UI Polish**
  - [ ] Verifikasi seluruh tombol dan kursi memiliki touch target minimal 48x48 dp.
  - [ ] Pastikan seluruh warna teks memenuhi rasio kontras WCAG AA yang telah dihitung (Light Primary `#825000`, State Error `#FF6B6B`).
  - [ ] Periksa kelengkapan `contentDescription` pada semua gambar dan ikon.
  - *Commit*: `style(design-system): polish accessibility touch targets and wcag contrast`
- [ ] **6.3 Portofolio Showcase & README**
  - [ ] Susun `README.md` utama dengan arsitektur visual, diagram alur transaksi atomik, dan panduan konfigurasi `local.properties`.
  - *Commit*: `docs: finalize portfolio documentation and readme`

> **Definition of Done (DoD) Fase 6:**  
> Seluruh unit test lulus (*green*), tidak ada memory leak atau crash pada skenario offline, dan kode siap dipresentasikan di GitHub/portofolio.
