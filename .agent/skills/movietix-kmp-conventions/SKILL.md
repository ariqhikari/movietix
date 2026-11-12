---
name: movietix-kmp-conventions
description: Konvensi arsitektur, struktur folder, dan Git commit untuk project MovieTix (Kotlin Multiplatform, Amper). Gunakan skill ini SETIAP KALI menulis, mengubah, atau mereview kode non-UI di project ini, termasuk repository, use case, ViewModel, Contract, Koin module, Ktor client, DataStore, mapper, model, error handling, navigasi, test, dan setiap kali akan membuat git commit, walaupun user tidak menyebut kata "arsitektur". Menjaga kode tiap fase tetap seragam, rapi, dan maintainable.
---

# MovieTix KMP Conventions

Aturan ini berlaku untuk semua kode dan commit di project MovieTix. Kode harus konsisten antar fase, mudah di-test, dan mudah dijelaskan saat review portfolio.

## Sumber kebenaran

1. Baca `PLAN.md`, `docs/ARCHITECTURE.md`, `docs/DATA_LAYER.md`, dan `docs/TASKS.md` sebelum mengerjakan task. Struktur folder, nama package, dan versi dependency di sana adalah acuan utama.
2. Kerjakan task sesuai urutan fase di `docs/TASKS.md`, lalu centang task yang selesai.
3. Jika dokumen ambigu atau bertentangan dengan kenyataan di repo, tulis pertanyaannya di jawaban. Jangan berasumsi diam-diam.
4. Jika perlu menyimpang dari dokumen, jelaskan alasannya dan perbarui dokumennya di commit yang sama.

## Build tool dan source set

- Project memakai **Amper** (`module.yaml`). Kode common ada di `src/`, kode spesifik platform di `src@android/` dan `src@ios/`.
- Package root: `org.ariqhikari.movietix`.
- Jangan menambahkan plugin atau konfigurasi Gradle tanpa memastikan Amper mendukungnya. Khusus token TMDB, gunakan `Secrets.TMDB_ACCESS_TOKEN` di `core/data/network/Secrets.kt` (file ter-gitignore) dengan `Secrets.kt.template` di repository.

## Struktur folder: package-by-layer

```
org/ariqhikari/movietix/
├── App.kt
├── core/            # fondasi bersama: network, storage, AppResult/AppError, theme, komponen UI umum
├── data/            # movie/ cinema/ booking/ ticket/ (remote, local, mapper, repository)
├── domain/          # movie/ cinema/ booking/ ticket/ (model, repository, usecase)
├── presentation/    # movie/ cinema/ booking/ ticket/ (screen, ViewModel, Contract)
├── di/              # CoreModule, MovieModule, CinemaModule, BookingModule, TicketModule, KoinInit
└── navigation/      # AppNavigation, ScreenDestination
```

Aturan struktur:
- Layer teratas tetap `core`, `data`, `domain`, `presentation`, `di`. Jangan diubah ke package-by-feature.
- Di dalam `data`, `domain`, dan `presentation`, subfolder fitur bernama sama: `movie`, `cinema`, `booking`, `ticket`.
- Kode yang dipakai >= 2 fitur masuk `core/`. Kode yang hanya relevan untuk satu fitur dilarang masuk `core/`.
- `presentation/<fitur>` tidak boleh memanggil ViewModel atau state internal fitur lain. Komunikasi antar layar lewat parameter rute di `ScreenDestination`.
- Arah dependency: `presentation -> domain <- data`. `domain` murni Kotlin: tidak boleh mengimpor Ktor, DataStore, Compose, atau library platform.

## Aturan inti

**Dependency Injection (Koin)**
- Semua dependency lewat constructor injection dan interface, bukan implementasi konkret. Dilarang membuat instance dependency langsung di dalam kelas.
- Satu modul Koin per area (`CoreModule`, `MovieModule`, `CinemaModule`, `BookingModule`, `TicketModule`), dikumpulkan di `KoinInit.kt` yang dipanggil dari entry point Android dan iOS. Inisialisasi Koin menggunakan `KoinPlatformTools.defaultContext()` untuk kompatibilitas multiplatform.
- Parameter `isDebug: Boolean` diteruskan ke `startMovieTixKoin(isDebug = ...)` dari platform entry point (`MainActivity` via `ApplicationInfo.FLAG_DEBUGGABLE`, `ViewController` via `Platform.isDebugBinary`).
- ViewModel didaftarkan dengan DSL ViewModel milik Koin. Dilarang service locator (`get()` / `inject()`) di dalam business logic.

**Pemisahan model**
- DTO (`*Dto`, remote), Entity (`*Entity`, DataStore), dan Domain Model adalah tiga tipe terpisah dengan mapper eksplisit (`toDomain()` / `toEntity()`).
- Dilarang membocorkan DTO atau Entity ke `domain` dan `presentation`.

**Repository dan use case**
- Interface repository ada di `domain/<fitur>/repository`, implementasi di `data/<fitur>/repository`.
- Satu use case = satu tanggung jawab, dipanggil lewat `operator fun invoke(...)`.
- ViewModel memanggil use case. Pengecualian (passthrough 1:1 ke repository) harus dicatat di ARCHITECTURE.md.
- Aksi yang melibatkan lebih dari satu fitur diorkestrasi di use case domain, bukan di presentation atau data.

**Pemesanan tiket (atomisitas)**
- Gunakan `ConfirmBookingUseCase` untuk alur: `reserveSeats` lalu `saveTicket`.
- Jika `saveTicket` gagal, wajib `releaseSeats` sebagai rollback. Hasil `releaseSeats` tidak boleh diabaikan: jika rollback gagal, log dan kembalikan error yang jelas.
- `reserveSeats` harus memeriksa dan menandai kursi dalam satu operasi atomik (satu `dataStore.edit`), agar tidak balapan.
- Pastikan urutan alur (hold kursi, pembayaran dummy, simpan tiket) konsisten dengan dokumen, dan tidak ada kondisi "sudah dibayar tetapi kursi gagal dipesan".

**Error handling**
- Gunakan `AppResult<T>` (`Success` / `Error`) dan hierarki `AppError` (Network, Storage, Booking, Unknown) dari `core/domain/model`. Jangan `throw` di boundary bisnis.
- Exception dari Ktor dan DataStore ditangkap di layer `data` lalu diterjemahkan ke `AppError`.
- Jangan menelan exception tanpa log atau state error. Jangan me-catch `CancellationException`.

**Presentation (UDF dengan Contract)**
- Satu `XxxContract` per screen berisi `State` (data class immutable), `Action` (sealed interface), dan `Effect` (sealed interface).
- ViewModel menurunkan `androidx.lifecycle.ViewModel`, memegang `MutableStateFlow<State>` privat yang diekspos sebagai `StateFlow`, menerima `Action` lewat satu fungsi `onAction(...)`, dan mengirim `Effect` lewat `Channel` yang diekspos sebagai `Flow`.
- Composable screen stateless terhadap logic: state masuk, action keluar. Efek sekali jalan (navigasi, snackbar) tidak disimpan di `State`.

**Networking (Ktor)**
- Satu `HttpClient` dari `createHttpClient(...)` di `CoreModule`: HTTPS, base `https://api.themoviedb.org/3/`, ContentNegotiation (kotlinx.serialization), timeout, dan `Authorization: Bearer <token>` di header.
- Logging dikendalikan flag `isDebug`, header `Authorization` wajib disanitasi, dan level `NONE` di release build.
- Endpoint dan base URL dikumpulkan di satu tempat.

**Local storage (DataStore Preferences)**
- Akses DataStore hanya lewat kelas data source lokal, tidak langsung dari repository atau ViewModel.
- Serialisasi JSON via kotlinx.serialization dengan `schema_version`.
- Baca-ubah-tulis lewat `edit`/`updateData` agar atomik.
- `AndroidContextProvider.init(context)` di `MainActivity.onCreate` dicatat sebagai technical debt sementara sampai Amper mendukung auto-initialization.

**Rahasia**
- Token TMDB (`Secrets.TMDB_ACCESS_TOKEN`) berasal dari file `Secrets.kt` di `commonMain` yang di-gitignore. Jangan hardcode ke file terlacak, jangan commit, jangan cetak di log. Pertahankan `Secrets.kt.template` di repo.

**Multiplatform**
- `expect/actual` hanya untuk dua hal: `getHttpEngine()` dan `createDataStorePath(...)`. Menambah `expect/actual` baru butuh alasan tertulis di ARCHITECTURE.md.
- Haptic lewat `LocalHapticFeedback` di common. Fitur simpan gambar tiket, tambah ke kalender, dan bagikan di luar scope (future work).
- Dilarang SwiftUI. iOS hanya host untuk Compose Multiplatform.
- Waktu: `kotlinx.datetime`. Concurrency: coroutines + Flow, dispatcher diinjeksi, tanpa `GlobalScope`.

## Penamaan

- Interface repository tanpa prefix `I`, implementasi berakhiran `Impl` (`MovieRepository` / `MovieRepositoryImpl`).
- Nama repository bioskop: `CinemaScheduleRepository` (bukan `CinemaRepository`).
- DTO berakhiran `Dto`, entity lokal `Entity`, kontrak screen `XxxContract`, ViewModel `XxxViewModel`.
- Use case berupa kata kerja: `GetNowPlayingMoviesUseCase`, `ConfirmBookingUseCase`.
- Satu file untuk satu kelas utama, nama file sama dengan nama kelas.

## Testing

- Use case, repository, ViewModel, mapper, dan `ProceduralScheduleEngine` harus bisa diuji di `commonTest` dengan fake (`FakeMovieRepository`, `FakeCinemaScheduleRepository`, `FakeTicketRepository`), bukan mock yang rapuh.
- Test wajib untuk: determinisme `ProceduralScheduleEngine`, `ConfirmBookingUseCase` (sukses dan rollback), kalkulasi harga, mapper dengan data null.
- ViewModel diuji dengan Turbine untuk `State` dan `Effect`.

## Git commit: Conventional Commits v1.0.0

Semua commit wajib mengikuti https://www.conventionalcommits.org/en/v1.0.0/

- Format: `<type>(<scope>): <description>`, body dan footer opsional.
- Type: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
- Scope resmi: `core`, `movie`, `cinema`, `booking`, `ticket`, `di`, `design-system`, `deps`, `navigation`. Pilih scope yang paling sesuai dengan perubahan utama.
- Description: huruf kecil, imperatif, tanpa titik di akhir, maksimal 72 karakter.
- Breaking change: tambahkan `!` setelah type/scope, atau footer `BREAKING CHANGE:`.
- Satu commit = satu perubahan logis. Commit setelah satu task di `docs/TASKS.md` selesai dan build/test lulus. Jangan menggabungkan beberapa task dalam satu commit.
- Gunakan saran commit message di `docs/TASKS.md` sebagai acuan, sesuaikan jika perubahan nyata berbeda.
- Jangan commit `local.properties`, token, atau file rahasia. Periksa `git status` sebelum commit.
- Jangan `git push` kecuali diminta.

Contoh: `feat(cinema): implement deterministic procedural schedule engine`

## Checklist sebelum selesai

- [ ] Layer `domain` tidak mengimpor library framework
- [ ] Semua dependency lewat constructor dan interface, terdaftar di modul Koin
- [ ] DTO, Entity, Domain Model terpisah dengan mapper eksplisit
- [ ] Error memakai `AppResult` / `AppError`, tidak ada exception mentah ke UI
- [ ] Contract (State, Action, Effect) dipakai, tidak ada logic bisnis di composable
- [ ] Tidak ada token atau secret di kode, log, atau commit
- [ ] File ditempatkan sesuai struktur layer dan subfolder fitur
- [ ] Test (fake) ditambahkan atau diperbarui
- [ ] Dokumen (`ARCHITECTURE.md`, `DATA_LAYER.md`, `TASKS.md`) diperbarui bila ada perubahan keputusan, dan task yang selesai dicentang
- [ ] Commit message valid menurut Conventional Commits

## Bila ragu

Pilih solusi yang lebih sederhana dan mudah di-test. Jangan menambah abstraksi baru (base class, generic wrapper, layer tambahan) kecuali ada masalah nyata yang diselesaikannya, dan jelaskan alasannya di jawaban.
