# MovieTix — Design System & Visual Specification

Dokumen ini mendefinisikan bahasa visual, sistem komponen, dan panduan pengalaman pengguna (UX) untuk **MovieTix**. Desain ini dirancang dengan karakter visual yang kuat, menolak estetika klise (*AI slop*), dan terinspirasi dari pengalaman otentik bioskop fisik dan tiket analog.

---

## 1. Konsep Visual & Art Direction: "Midnight Cinema & Ticket Craft"

> **Art Direction Statement:**  
> MovieTix mengusung tema **"Midnight Cinema & Ticket Craft"** — sebuah harmoni visual antara kemewahan studio bioskop malam hari dengan kehangatan nostalgia tiket bioskop fisik era analog. Antarmuka didominasi oleh kanvas *Obsidian Navy* yang dalam dan hening layaknya ruang teater saat lampu dipadamkan, dipadukan dengan aksen emas tembaga (*Warm Amber / Brass*) yang memancarkan pendar lampu proyektor dan plakat bioskop premium. Elemen tiket mengadopsi metafora fisik nyata: lekukan perforasi stempel (*punched notch*), garis sobekan putus-putus (*tear-off dashed line*), serta tipografi editorial yang tegas. Hasilnya adalah antarmuka yang elegan, berbobot, taktual, dan memperlakukan setiap pemesanan tiket layaknya sebuah tiket masuk eksklusif ke pengalaman sinematik, bukan sekadar formulir transaksi e-commerce generik.

---

## 2. Palet Warna & Verifikasi Kontras WCAG 2.1 (Hasil Script)

MovieTix dirancang dengan **Dark Theme sebagai pengalaman primer (default)** untuk merefleksikan suasana sinema, didampingi oleh **Light Theme (Matinee Mode)** yang bersih dan berkarakter.

### 2.1 Rumus Perhitungan Kontras WCAG 2.1
Sesuai standar W3C WCAG 2.1, seluruh angka kontras dihitung secara matematis menggunakan rumus:
1. **Normalisasi sRGB ke Linier ($C_{\text{linear}}$)** untuk setiap komponen warna $C \in \{R, G, B\}$ ternormalisasi $0.0 \dots 1.0$:
   $$C_{\text{linear}} = \begin{cases} \frac{C}{12.92}, & \text{jika } C \le 0.04045 \\ \left(\frac{C + 0.055}{1.055}\right)^{2.4}, & \text{jika } C > 0.04045 \end{cases}$$
2. **Relative Luminance ($L$)**:
   $$L = 0.2126 \times R_{\text{linear}} + 0.7152 \times G_{\text{linear}} + 0.0722 \times B_{\text{linear}}$$
3. **Contrast Ratio ($C$)**:
   $$\text{Contrast Ratio} = \frac{L_1 + 0.05}{L_2 + 0.05} \quad (\text{dengan } L_1 > L_2)$$

Ambang batas WCAG AA:
- **Teks Normal (< 18pt / 24px atau < 14pt tebal)**: Minimal **4.5:1**
- **Teks Besar (>= 18pt / 24px atau >= 14pt tebal)** & Komponen UI: Minimal **3.0:1**

---

### 2.2 Dark Theme (Primary Showcase — "Midnight Theater")

| Token Name | Hex Code | Peran / Penggunaan | Rasio Kontras Terverifikasi | Status WCAG |
|---|---|---|---|---|
| `cinema-background` | `#0B0E14` | Kanvas dasar layar (Obsidian Navy malam hari) | Base Canvas | - |
| `cinema-surface-subtle` | `#131822` | Surface tingkat 1 (Card background, App Bar) | Kontras vs `#0B0E14` = **1.25:1** | Tonal Layering |
| `cinema-surface-card` | `#1A202E` | Surface tingkat 2 (Elevated card, Bottom Sheet) | Kontras vs `#0B0E14` = **1.56:1** | Tonal Layering |
| `cinema-surface-border` | `#2A3448` | Garis batas tipis kartu, divider & notch | 1dp stroke delimiter | Boundary Stroke |
| `cinema-primary` | `#E5A93C` | Aksen utama (Amber Gold: CTA, waktu aktif) | vs `#0B0E14` = **9.26:1**<br>vs `#1A202E` = **7.81:1** | Pass AAA (All) |
| `cinema-primary-variant`| `#C68B23` | State pressed pada elemen primary | vs `#0B0E14` = **7.08:1** | Pass AAA (All) |
| `cinema-secondary` | `#F08C4A` | Aksen pelengkap (Neon Ember: badge genre) | vs `#0B0E14` = **7.87:1** | Pass AAA (All) |
| `cinema-on-primary` | `#120C02` | Teks/ikon di atas warna emas `cinema-primary` | vs `#E5A93C` = **9.33:1** | Pass AAA (All) |
| `cinema-text-primary` | `#F2F5FA` | Judul, harga, nama film, label penting | vs `#0B0E14` = **17.68:1**<br>vs `#1A202E` = **14.90:1** | Pass AAA (All) |
| `cinema-text-secondary`| `#9AA7BC` | Sinopsis, info durasi, metadata film | vs `#0B0E14` = **7.94:1**<br>vs `#1A202E` = **6.69:1** | Pass AA (Normal) |
| `cinema-text-tertiary` | `#63738A` | Label nonaktif, hint, seat disabled | vs `#0B0E14` = **4.00:1** | Pass AA (Large/UI) |
| `cinema-seat-available`| `#222B3D` | Kursi tersedia (Background netral) | Border stroke `#3D4C69` | UI Component |
| `cinema-seat-selected` | `#E5A93C` | Kursi dipilih user (Gold solid + glyph) | vs `#120C02` = **9.33:1** | Pass AAA |
| `cinema-seat-occupied` | `#161B26` | Kursi terisi (Dark navy redup + ikon 'X') | Texture & Glyph khusus | Multi-modal |
| `state-success` | `#2EC492` | Konfirmasi pembayaran sukses, status lunas | vs `#0B0E14` = **8.67:1** | Pass AAA (All) |
| `state-error` | `#FF6B6B` | Pesan error, pembatalan, network fail | vs `#0B0E14` = **6.96:1**<br>vs `#1A202E` = **5.87:1** | Pass AA (Normal) |

> **Perbaikan Penting Dark Theme**: Warna `state-error` diperbarui dari `#E5484D` (rasio lama 4.16:1 di atas card, gagal AA) menjadi **`#FF6B6B`** (Coral Red Vivid), sehingga mencapai rasio **5.87:1** di atas `cinema-surface-card` `#1A202E`, lolos mutlak WCAG AA untuk teks normal.

---

### 2.3 Light Theme (Matinee Mode — "Silver Screen Morning")

| Token Name | Hex Code | Peran / Penggunaan | Rasio Kontras Terverifikasi | Status WCAG |
|---|---|---|---|---|
| `light-background` | `#F8F9FC` | Background layar terang | Base Canvas | - |
| `light-surface` | `#FFFFFF` | Surface kartu dan app bar | Clean white | - |
| `light-surface-subtle` | `#EDF1F7` | Surface kontainer pencarian, input field | vs `#FFFFFF` = **1.14:1** | Tonal Layering |
| `light-primary` | `#825000` | Aksen emas gelap untuk teks & CTA di light theme | vs `#FFFFFF` = **6.78:1**<br>vs `#F8F9FC` = **6.44:1**<br>vs `#EDF1F7` = **5.98:1** | Pass AA (Normal)<br>Pass AAA (Large) |
| `light-on-primary` | `#FFFFFF` | Teks di atas tombol emas `#825000` | vs `#825000` = **6.78:1** | Pass AA (Normal) |
| `light-text-primary` | `#10141D` | Judul utama dan teks esensial | vs `#FFFFFF` = **18.42:1** | Pass AAA (All) |
| `light-text-secondary` | `#526075` | Sinopsis dan metadata film | vs `#FFFFFF` = **6.39:1**<br>vs `#EDF1F7` = **5.63:1** | Pass AA (Normal) |
| `light-border` | `#D6DEEB` | Garis pembatas dan perforasi tiket | Subtle delimiter | UI Boundary |
| `light-state-error` | `#C62828` | Teks dan icon error di tema terang | vs `#FFFFFF` = **5.62:1**<br>vs `#EDF1F7` = **4.96:1** | Pass AA (Normal) |

> **Perbaikan Penting Light Theme**: Nilai `light-primary` diperbarui dari `#A66B0D` (rasio lama 4.44:1 di atas putih, gagal AA < 4.5:1) menjadi **`#825000`** (Amber Ochre Deep). Warna baru ini menghasilkan rasio kontras **6.78:1** di atas putih murni, **6.44:1** di atas background, dan **5.98:1** di atas surface subtle, sehingga lolos penuh WCAG AA untuk teks normal di seluruh variasi surface terang.

---

## 3. Tipografi

Tipografi menggunakan dua keluarga huruf Google Fonts bebas lisensi yang dibundel di `shared/composeResources/font/`:
1. **Display Font: `Outfit`**  
   Font geometris dengan kurva modern, proporsi lebar yang sinematis, dan bobot *Bold/ExtraBold* untuk judul film, kode booking, dan header layar. Dilengkapi file lisensi `OFL_Outfit.txt`.
2. **Body Font: `Plus Jakarta Sans`**  
   Font modern berdaya baca tinggi (*high legibility*), memiliki proporsi angka yang sangat bersih untuk jam tayang, baris/kolom kursi, dan nominal mata uang rupiah. Menggunakan single variable font `plusjakartasans_variable.ttf` dengan konfigurasi `FontVariation.Settings(FontVariation.weight(...))` untuk bobot 400 (Regular), 500 (Medium), 600 (SemiBold), dan 700 (Bold). Dilengkapi file lisensi `OFL_PlusJakartaSans.txt`.

### Skala Tipografi (Typography Scale)

| Style Token | Font Family | Weight | Size (sp) | Line Height (sp) | Tracking | Penggunaan |
|---|---|---|---|---|---|---|
| `Display Large` | Outfit | ExtraBold (800) | 32 | 38 | -0.5px | Hero Banner Title, Sukses Booking |
| `Display Medium` | Outfit | Bold (700) | 26 | 32 | -0.3px | Judul Film di Layar Detail |
| `Display Small` | Outfit | SemiBold (600) | 22 | 28 | 0px | Header Screen (e.g., "Pilih Kursi") |
| `Headline Medium`| Outfit | SemiBold (600) | 18 | 24 | 0px | Nama Bioskop, Nomor Studio, Section Title |
| `Title Medium` | Plus Jakarta Sans | SemiBold (600) | 16 | 22 | +0.1px | Judul Film di Kartu Grid, Tab Title |
| `Body Large` | Plus Jakarta Sans | Regular (400) | 15 | 22 | +0.2px | Sinopsis film, detail instruksi |
| `Body Medium` | Plus Jakarta Sans | Medium (500) | 14 | 20 | +0.2px | Jam tayang, harga per tiket, metode bayar |
| `Body Small` | Plus Jakarta Sans | Regular (400) | 12 | 16 | +0.3px | Nama pemeran/karakter, catatan kaki |
| `Label Large` | Plus Jakarta Sans | Bold (700) | 14 | 18 | +0.5px | Tombol CTA ("PILIH JADWAL", "BAYAR SEKARANG") |
| `Label Medium` | Plus Jakarta Sans | SemiBold (600) | 12 | 16 | +0.4px | Genre Tag Chip, Status Badge ("IMAX", "XXI") |
| `Label Mono` | Outfit (Numeric) | Bold (700) | 16 | 20 | +1.0px | Kode Booking ("MTX-84920"), Nomor Kursi ("C7") |

---

## 4. Sistem Spacing, Radius & Elevasi

### 4.1 Spacing Scale (Grid 4dp / 8dp)
- `spacing-xxs` : 4dp (Jarak antar ikon dan teks kecil, gap chip tag)
- `spacing-xs` : 8dp (Jarak antar elemen kursi di seat map, padding dalam badge)
- `spacing-sm` : 12dp (Jarak horizontal antar card dalam carousel)
- `spacing-md` : 16dp (Margin standar layar kiri-kanan, padding standar kartu)
- `spacing-lg` : 24dp (Jarak antar-seksi konten di detail film)
- `spacing-xl` : 32dp (Jarak seksi seat map ke tombol sticky CTA)
- `spacing-xxl`: 48dp (Jarak hero banner ke floating bar)

### 4.2 Radius Scale (Konsisten dengan Spesifikasi Komponen)
- `radius-sm` : 6dp (Chip genre, time slot pill)
- `radius-md` : 10dp (Kotak kursi bioskop, rating badge)
- `radius-lg` : 16dp (Poster card, detail card, dialog modal)
- `radius-pill`: 999dp (Tombol CTA bulat, status filter)
- *Catatan Khusus Tiket*: Kartu tiket menggunakan custom canvas/shape dengan radius sudut 16dp (`radius-lg`) ditambah **lingkaran coakan (notch) sedalam 12dp** pada kedua sisi divider horizontal.

---

## 5. Spesifikasi Komponen Kunci

### 5.1 Movie Poster Card
- **Aspect Ratio**: 2:3 vertikal standar poster film bioskop.
- **Corner Radius**: 16dp (`radius-lg`), konsisten dengan skala radius sistem.
- **Top Overlay**: Badge rating mengambang di pojok kiri atas dengan latar belakang semitransparan `#0B0E14` (opacity 85%), border `1dp #E5A93C`, ikon bintang emas solid, dan teks rating format `8.4`.
- **Bottom Scrim**: Gradien vertikal linear gelap dari transparan ke `#0B0E14` (opacity 90%) pada 30% area bawah poster untuk menjamin keterbacaan judul film dan tahun rilis.

### 5.2 Interactive Seat Map
Peta kursi mematuhi aturan aksesibilitas dan pembedaan multi-modal (tidak mengandalkan warna semata):

```
         ┌────────────────────────────────────────────────────────┐
         │                  LAYAR BIOSKOP (SCREEN)                │
         │          (Garis lengkung busur cekung + pendar)        │
         └────────────────────────────────────────────────────────┘

 [A]   [ . ] [ . ] [ . ] [ . ]     [ . ] [ . ] [ . ] [ . ]   [A]
 [B]   [ . ] [ . ] [ X ] [ X ]     [ . ] [ . ] [ . ] [ . ]   [B]
 [C]   [ . ] [ . ] [ . ] [ . ]     [ v ] [ v ] [ . ] [ . ]   [C]
 [D]   [ X ] [ X ] [ . ] [ . ]     [ . ] [ . ] [ . ] [ . ]   [D]
        1     2     3     4           5     6     7     8
```

#### Spesifikasi Status Kursi (Multi-Modal Indicators):
1. **Available (Tersedia)**:
   - Visual: Kotak 34x34dp, background `#222B3D`, border `1.5dp #3D4C69`, sudut melengkung 6dp.
   - Pembeda non-warna: Permukaan polos bersih, label nomor kursi kontras tinggi.
2. **Selected (Dipilih oleh User)**:
   - Visual: Kotak 34x34dp, background solid `#E5A93C` (Gold), glow emas lembut di tepian.
   - Pembeda non-warna: Muncul glyph ikon **Checkmark (`v`) kontras gelap** di tengah kursi, label baris-kolom berubah tebal (*Bold*).
3. **Occupied / Reserved (Sudah Dipesan Orang Lain)**:
   - Visual: Kotak 34x34dp, background gelap pekat `#161B26`, border `1dp #202736`.
   - Pembeda non-warna: Diarsir garis diagonal halus dan menampilkan ikon silang (**`X`**), `clickable = false`.

### 5.3 Perforated Cinema Ticket Stub
- **Bentuk Geometri**: Terbagi dua bagian: *Header/Body Info* (atas) dan *QR Voucher* (bawah).
- **Notch Perforasi**: Dua lengkungan setengah lingkaran coak ke dalam berdiameter 24dp pada sisi kiri dan kanan kartu `) (`.
- **Perforation Line**: Garis putus-putus (*dashed line*) sepanjang perbatasan notch dengan warna `#3D4C69`.
- **QR Code Area**: QR code berukuran 160x160dp di-render menggunakan library Compose Multiplatform `qrose` dengan kontras maksimal hitam di atas putih gading `#FFFFFF`.

---

## 6. Wireframe Teks Bebas Emoji (ASCII Only)

### 6.1 Layar Utama (Home Screen)

```text
+-------------------------------------------------------------+
| [MOVIETIX]                         [Kota: Jakarta Pusat v]  |
+-------------------------------------------------------------+
| NOW PLAYING PREMIERE                                        |
| +---------------------------------------------------------+ |
| | [Backdrop Film Utama - Carousel Besar]                  | |
| | DUNE: PART TWO                                          | |
| | Action, Sci-Fi * 2j 46m * [* 8.6]                       | |
| | [ Tombol Emas: Pesan Sekarang ]                         | |
| +---------------------------------------------------------+ |
| ( o  .  .  . ) Carousel Indicator                           |
+-------------------------------------------------------------+
| KATEGORI: [ (o) Sedang Tayang ] [ Populer ] [ Akan Datang ] |
+-------------------------------------------------------------+
| FILM TERPOPULER                                [Lihat Semua]|
| +---------------+  +---------------+  +---------------+     |
| | [ Poster 1 ]  |  | [ Poster 2 ]  |  | [ Poster 3 ]  |     |
| | [* 8.2]       |  | [* 7.9]       |  | [* 8.5]       |     |
| | Oppenheimer   |  | Wonka         |  | Spider-Man    |     |
| | Drama, Bio    |  | Fantasy       |  | Animation     |     |
| +---------------+  +---------------+  +---------------+     |
+-------------------------------------------------------------+
| [NAVBAR: (o) Film   |   [#] Tiket Saya   |   [=] Pengaturan]|
+-------------------------------------------------------------+
```

---

### 6.2 Layar Detail Film (Movie Detail Screen)

```text
+-------------------------------------------------------------+
| [ < Kembali ]                                               |
| +---------------------------------------------------------+ |
| | [ Backdrop Film Lebar dengan Gradien Hitam ke Bawah ]   | |
| |                                                         | |
| |   +-----------+   Dune: Part Two                        | |
| |   | Poster    |   2024 * 166 Menit * 13+                | |
| |   | Kecil     |   [* 8.6/10 (12.4k ulasan)]             | |
| |   +-----------+   Genre: Sci-Fi, Petualangan, Aksi      | |
| +---------------------------------------------------------+ |
+-------------------------------------------------------------+
| SINOPSIS                                                    |
| Paul Atreides bersatu kembali dengan Chani dan suku Fremen  |
| saat membalas dendam terhadap para konspirator... [Lebih]   |
+-------------------------------------------------------------+
| PEMERAN UTAMA (CAST)                                        |
| ( O ) Timothee C.   ( O ) Zendaya      ( O ) Rebecca F.     |
| Paul Atreides       Chani              Lady Jessica         |
+-------------------------------------------------------------+
| TRAILER RESMI                                               |
| [ Thumbnail Video Trailer + Ikon Putar [ > ] ]              |
+-------------------------------------------------------------+
| [STICKY BOTTOM BAR]                                         |
| Mulai dari Rp 45.000                  [ PILIH JADWAL & KURSI]
+-------------------------------------------------------------+
```

---

### 6.3 Layar Jadwal & Pemilihan Kursi (Schedule & Seat Map Screen)

```text
+-------------------------------------------------------------+
| [ < ] Pilih Jadwal & Kursi                     Dune: Part 2 |
+-------------------------------------------------------------+
| PILIH TANGGAL:                                              |
| [ RABU | 12 Nov ]  [(o) KAMIS | 13 Nov]  [ JUMAT | 14 Nov ] |
+-------------------------------------------------------------+
| PILIH BIOSKOP & JAM:                                        |
| Grand Cinema XXI (Mall Grand Indonesia)                     |
| Studio 1 (IMAX 2D) - Rp 65.000                              |
| [ 12:45 ]  [(o) 15:30]  [ 18:15 ]  [ 21:00 (Penuh) ]        |
+-------------------------------------------------------------+
|                       LAYAR BIOSKOP                         |
|           (  __________________________________  )          |
|                                                             |
|   A  [ . ][ . ][ . ][ . ]      [ . ][ . ][ . ][ . ]  A      |
|   B  [ . ][ . ][ X ][ X ]      [ . ][ . ][ . ][ . ]  B      |
|   C  [ . ][ . ][ . ][ . ]      [ v ][ v ][ . ][ . ]  C      |
|   D  [ X ][ X ][ . ][ . ]      [ . ][ . ][ . ][ . ]  D      |
|         1   2   3   4             5   6   7   8         |
|                                                             |
| LEGEND: [.] Tersedia   [v] Dipilih   [X] Terisi             |
+-------------------------------------------------------------+
| [BAR KASIR STICKY]                                          |
| Kursi: C5, C6 (2 Tiket)                                     |
| Total: Rp 130.000                    [ LANJUT KE PEMBAYARAN] |
+-------------------------------------------------------------+
```

---

### 6.4 Bottom Sheet Pembayaran Dummy

```text
+-------------------------------------------------------------+
|                         ===                                 |
| Ringkasan Pemesanan                                         |
| Film: Dune: Part Two (IMAX 2D)                              |
| Jadwal: Kamis, 13 Nov 2026 | 15:30                          |
| Kursi: C5, C6 (2 Kursi)                                     |
| ----------------------------------------------------------- |
| 2 x Tiket @ Rp 65.000                           Rp 130.000  |
| Biaya Layanan Aplikasi                             Rp 3.000  |
| TOTAL PEMBAYARAN                                Rp 133.000  |
| ----------------------------------------------------------- |
| METODE PEMBAYARAN (DUMMY):                                  |
| (o) [BCA] BCA Virtual Account                               |
| ( ) [GOPAY] GoPay E-Wallet                                  |
| ( ) [CARD] Kartu Debit / Kredit                             |
| ----------------------------------------------------------- |
| [ SIMULASI BAYAR SEKARANG (Rp 133.000) ]                    |
+-------------------------------------------------------------+
```

---

### 6.5 Layar Tiket & QR Code Offline (Ticket Detail Screen)

```text
+-------------------------------------------------------------+
| [ < ] E-Tiket Masuk Bioskop                                 |
+-------------------------------------------------------------+
| /---------------------------------------------------------\ |
| | MOVIETIX OFFICIAL PASS                    KODE: MTX-9281| |
| |                                                         | |
| | DUNE: PART TWO                                          | |
| | Grand Cinema XXI - Studio 1 (IMAX 2D)                   | |
| | Kamis, 13 November 2026 * 15:30 WIB                     | |
| | Baris / Kursi:  C5, C6                                  | |
| | Total Bayar  :  Rp 133.000 (LUNAS)                      | |
| ) - - - - - - - - - - - - - - - - - - - - - - - - - - - - ( |
| |                                                         | |
| |              +-----------------------+                  | |
| |              |  | | | || ||| | |||   |                  | |
| |              |  QR CODE OFFLINE      |                  | |
| |              |  HIGH CONTRAST QROSE  |                  | |
| |              +-----------------------+                  | |
| |             Scan kode ini di pintu masuk studio          | |
| \---------------------------------------------------------/ |
|                                                             |
| [Status: Tiket Aktif - Dapat Digunakan Offline]             |
+-------------------------------------------------------------+
```

---

## 7. Prinsip Gerakan & Haptic Feedback

1. **Micro-interaction Kursi & Haptic Feedback**:
   - Saat kursi di-tap, kursi melakukan efek *spring bounce* skala `0.85 -> 1.15 -> 1.0` (durasi 150ms).
   - Umpan balik haptic di-trigger menggunakan API bersama Compose Multiplatform:  
     `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)`  
     Pendekatan ini berjalan 100% di `commonMain` tanpa memerlukan deklarasi `expect/actual`.
2. **Ticket Entrance Transition**:
   - Layar tiket muncul dari bawah dengan kurva *OvershootEasing* menyerupai tiket yang dikeluarkan oleh mesin cetak otomatis bioskop.
3. **Fitur yang Dikeluarkan dari Scope (Future Work)**:
   - Fitur "Simpan Gambar Tiket ke Galeri", "Tambah ke Kalender Sistem", dan "Bagikan Gambar" dikeluarkan dari ruang lingkup inti MovieTix agar tidak memerlukan bridge native platform-specific yang berlebihan.
