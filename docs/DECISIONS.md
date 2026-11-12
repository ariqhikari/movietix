# Architecture Decision Records (ADRs)

Dokumen ini mencatat keputusan arsitektur kunci (Architecture Decision Records) yang diambil dalam pengembangan proyek **MovieTix**.

---

## ADR-001: Amper Project System with Standalone Kotlin Wrapper

- **Status**: Diterima (Accepted)
- **Konteks**:
  Pengembangan proyek Kotlin Multiplatform (KMP) yang menargetkan Android dan iOS sering kali memerlukan konfigurasi build Gradle yang kompleks, verbose, dan rentan terhadap desinkronisasi antar-target.
- **Keputusan**:
  Menggunakan JetBrains **Amper** dengan file deklaratif `module.yaml`, integrasi Version Catalog `libs.versions.toml`, dan script wrapper mandiri `./kotlin`.
- **Konsekuensi**:
  - Konfigurasi dependensi dan platform targets sangat ringkas dan deklaratif.
  - Perintah build dan test terpadu (`./kotlin test -p android -m shared` dan `./kotlin test -p iosSimulatorArm64 -m shared`).
  - Tidak memerlukan skrip Gradle Groovy/KTS kustom di dalam module `shared`.

---

## ADR-002: ProceduralScheduleEngine for Deterministic Cinema Schedules & Occupied Seats

- **Status**: Diterima (Accepted)
- **Konteks**:
  API publik The Movie Database (TMDB) menyediakan katalog film, sinopsis, cast, dan trailer, namun tidak menyediakan API jadwal bioskop lokal Indonesia, jenis studio, harga tiket, maupun denah kursi real-time. Membutuhkan backend server terpisah akan menambah ketergantungan jaringan dan biaya operasional untuk proyek portfolio.
- **Keputusan**:
  Membangun mesin prosedural lokal deterministik (`ProceduralScheduleEngine`) murni Kotlin di layer data:
  - Menggunakan fungsi hash deterministik berbasis kombinasi `(movieId, date, cinemaId, studioId)`.
  - Mensimulasikan jaringan bioskop Indonesia nyata: CGV, Cinema XXI, Cinepolis, dan FLIX Cinema dengan tipe studio Regular 2D, Velvet Class, IMAX 3D, Premiere, dan Gold Class.
  - Menghasilkan sebaran kursi terisi realistis (clustering di baris tengah dan belakang).
- **Konsekuensi**:
  - Seluruh alur jadwal, pemilihan kursi, dan harga dapat diuji secara mandiri (offline-first).
  - Hasil jadwal dan kursi 100% konsisten antar-buka aplikasi dan dapat diverifikasi via golden test (`ProceduralScheduleEngineTest`).

---

## ADR-003: Jetpack DataStore Preferences for Local Persistence

- **Status**: Diterima (Accepted)
- **Konteks**:
  Aplikasi memerlukan penyimpanan lokal untuk tiket yang telah dipesan dan status kursi yang telah dibooking oleh pengguna, yang harus bertahan saat aplikasi ditutup dan dibuka kembali.
- **Keputusan**:
  Menggunakan **AndroidX Jetpack DataStore Preferences** Multiplatform (`androidx.datastore:datastore-preferences-core`) dikombinasikan dengan **Kotlinx Serialization**:
  - Reservasi kursi pengguna disimpan per-jadwal dengan key `occupied_seats_<scheduleId>`.
  - Daftar tiket disimpan sebagai JSON string ter-enkripsi di key `booked_tickets_list` dengan pembatasan maksimal 50 tiket terbaru (FIFO).
  - Exception handling otomatis yang melakukan fallback ke list kosong jika payload JSON korup tanpa memicu crash aplikasi.
- **Konsekuensi**:
  - Reaktif murni melalui Kotlin Coroutine `Flow<Preferences>`.
  - Transaksional dan thread-safe tanpa memerlukan engine database SQLite/Room yang berat.

---

## ADR-004: Pure Kotlin commonTest Multiplatform Testing Strategy

- **Status**: Diterima (Accepted)
- **Konteks**:
  Memastikan kualitas logika bisnis di Android dan iOS tanpa menulis kode pengujian terpisah di masing-masing platform.
- **Keputusan**:
  Menempatkan 100% rangkaian pengujian di source set `commonTest` (`shared/test/`):
  - Menggunakan Test Fakes (`FakeCinemaScheduleRepository`, `FakeTicketRepository`, `FakePaymentGateway`) daripada framework mocking berbasis JVM bytecode generation (seperti Mockito).
  - Menggunakan **Turbine** untuk assertion Kotlin Flow dan `StandardTestDispatcher` untuk pengujian waktu deterministik.
  - Memasukkan integration test menyeluruh (`BookingFlowIntegrationTest`) yang menguji integrasi engine nyata, use case, dan DataStore in-memory.
- **Konsekuensi**:
  - 121 automated unit & integration tests lulus secara identik di Android JVM dan iOS Simulator Native Arm64.
  - Tidak ada dependensi JVM khusus dalam test suite inti.

---

## ADR-005: Strict Token-Driven Design System ("Midnight Cinema & Ticket Craft")

- **Status**: Diterima (Accepted)
- **Konteks**:
  UI aplikasi pemesanan tiket bioskop membutuhkan identitas visual yang imersif dan elegan, sekaligus wajib memenuhi standar aksesibilitas kontras W3C WCAG 2.1 Level AA/AAA.
- **Keputusan**:
  Mengembangkan design system terpusat dengan estetika "Midnight Cinema & Ticket Craft":
  - Skema warna Dark Theme berbasis Obsidian Navy (`#0B0E14`), kartu elevasi `#1A202E`, aksen Amber Gold (`#E5A93C`), dan aksen Neon Ember (`#F08C4A`).
  - Seluruh warna didefinisikan secara tunggal di `MovieTixColors` dan dilarang keras menggunakan `Color(0x...)` langsung di presentation screen (diverifikasi otomatis oleh `scripts/audit_codebase.sh`).
  - Seluruh rasio kontras diverifikasi dengan skrip matematis `scripts/calculate_contrast.py` dan didokumentasikan di `docs/ACCESSIBILITY.md`.
- **Konsekuensi**:
  - Rasio kontras teks utama mencapai 17.68:1 (melampaui syarat WCAG AAA 7:1).
  - Konsistensi tampilan 100% seragam di seluruh screen dan komponen.
