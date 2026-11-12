# MovieTix Accessibility & WCAG 2.1 Audit Report

Dokumen ini mendokumentasikan kepatuhan aksesibilitas aplikasi **MovieTix** terhadap standar **W3C Web Content Accessibility Guidelines (WCAG) 2.1 Level AA & AAA**, standar Material Design accessibility, serta pedoman platform Android (TalkBack) dan iOS (VoiceOver).

---

## 1. Color Contrast Ratios (WCAG 2.1 Formula)

Semua rasio kontras dihitung secara matematis menggunakan rumus luminansi relatif W3C:
$$\text{Luminance} = 0.2126 \times R_{\text{linear}} + 0.7152 \times G_{\text{linear}} + 0.0722 \times B_{\text{linear}}$$
$$\text{Contrast Ratio} = \frac{L_1 + 0.05}{L_2 + 0.05} \quad (\text{dengan } L_1 \ge L_2)$$

Dapat diverifikasi ulang menggunakan skrip:
```bash
python3 scripts/calculate_contrast.py
```

### Hasil Audit Rasio Kontras

| Theme | Foreground Token | Hex | Background Token | Hex | Contrast Ratio | WCAG 2.1 AA | Purpose |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Dark | `CinemaTextPrimary` | `#F2F5FA` | `CinemaBackground` | `#0B0E14` | **17.68:1** | PASS (WCAG AAA >= 7.0:1) | Headings / Body on canvas |
| Dark | `CinemaTextPrimary` | `#F2F5FA` | `CinemaSurfaceCard` | `#1A202E` | **14.90:1** | PASS (WCAG AAA >= 7.0:1) | Headings / Body on cards |
| Dark | `CinemaTextSecondary` | `#9AA7BC` | `CinemaBackground` | `#0B0E14` | **7.94:1** | PASS (WCAG AAA >= 7.0:1) | Secondary metadata on canvas |
| Dark | `CinemaTextSecondary` | `#9AA7BC` | `CinemaSurfaceCard` | `#1A202E` | **6.69:1** | PASS (WCAG AA >= 4.5:1) | Secondary metadata on cards |
| Dark | `CinemaTextHint` | `#7E8EAA` | `CinemaSurfaceCard` | `#1A202E` | **4.91:1** | PASS (WCAG AA >= 4.5:1) | Hint / placeholder on cards |
| Dark | `CinemaTextTertiary` | `#63738A` | `CinemaSurfaceCard` | `#1A202E` | **3.37:1** | PASS (UI Component >= 3.0:1) | Inactive tags / decorative text |
| Dark | `CinemaOnPrimary` | `#120C02` | `CinemaPrimary` | `#E5A93C` | **9.33:1** | PASS (WCAG AAA >= 7.0:1) | Text/Icon on Primary CTA button |
| Dark | `CinemaPrimary` | `#E5A93C` | `CinemaBackground` | `#0B0E14` | **9.26:1** | PASS (WCAG AA Large >= 3.0:1) | Primary Gold titles / large numbers |
| Dark | `CinemaPrimary` | `#E5A93C` | `CinemaPrimaryContainer` | `#362103` | **7.32:1** | PASS (WCAG AAA >= 7.0:1) | Gold icon inside Deep Amber container |
| Dark | `CinemaTextPrimary` | `#F2F5FA` | `CinemaPrimaryContainer` | `#362103` | **13.97:1** | PASS (WCAG AAA >= 7.0:1) | Text inside Deep Amber container |
| Dark | `StateSuccess` | `#2EC492` | `CinemaBackground` | `#0B0E14` | **8.67:1** | PASS (WCAG AAA >= 7.0:1) | Success state label on canvas |
| Dark | `StateErrorDark` | `#FF6B6B` | `CinemaBackground` | `#0B0E14` | **6.96:1** | PASS (WCAG AA >= 4.5:1) | Error message on canvas |
| Dark | `StateErrorDark` | `#FF6B6B` | `CinemaSurfaceCard` | `#1A202E` | **5.87:1** | PASS (WCAG AA >= 4.5:1) | Error message on cards |
| Dark | `CinemaSeatBorder` | `#506488` | `CinemaBackground` | `#0B0E14` | **3.24:1** | PASS (UI Component >= 3.0:1) | Available seat outline contrast |
| Light | `LightTextPrimary` | `#10141D` | `LightBackground` | `#F8F9FC` | **17.50:1** | PASS (WCAG AAA >= 7.0:1) | Headings / Body on light canvas |
| Light | `LightTextPrimary` | `#10141D` | `LightSurface` | `#FFFFFF` | **18.42:1** | PASS (WCAG AAA >= 7.0:1) | Headings / Body on white card |
| Light | `LightTextSecondary` | `#526075` | `LightSurface` | `#FFFFFF` | **6.39:1** | PASS (WCAG AA >= 4.5:1) | Secondary text on white card |
| Light | `LightTextHint` | `#5D6D82` | `LightSurface` | `#FFFFFF` | **5.28:1** | PASS (WCAG AA >= 4.5:1) | Placeholder on light card |
| Light | `LightOnPrimary` | `#FFFFFF` | `LightPrimary` | `#825000` | **6.78:1** | PASS (WCAG AA >= 4.5:1) | White text on Light Primary button |
| Light | `LightPrimary` | `#825000` | `LightBackground` | `#F8F9FC` | **6.44:1** | PASS (WCAG AA >= 4.5:1) | Primary ochre accents on light canvas |
| Light | `LightSecondary` | `#A34400` | `LightSurface` | `#FFFFFF` | **6.21:1** | PASS (WCAG AA >= 4.5:1) | Secondary terracotta on white card |
| Light | `StateErrorLight` | `#C62828` | `LightSurface` | `#FFFFFF` | **5.62:1** | PASS (WCAG AA >= 4.5:1) | Error crimson on white card |

---

## 2. Touch Target Sizing (Minimal 48x48dp)

Sesuai pedoman Android Accessibility & Apple Human Interface Guidelines:
- **Tombol CTA Utama**: Tinggi 52dp atau 48dp (`height(52.dp)` / `height(48.dp)`), memenuhi target sentuh minimal 48dp.
- **Tombol Navigasi Kembali (Back Button)**: Ukuran visual 40dp dengan area sentuh terpadu pada bar navigasi (padding 12dp menghasilkan touch envelope >= 48dp).
- **Date Picker Chips**: Tinggi item 64dp x lebar 52dp, melampaui batas minimal 48dp.
- **Showtime Chips**: Minimum 44dp tinggi dengan margin vertikal 8dp (`touch target padding` >= 48dp).
- **Seat Map Interactive Cells**: Setiap kursi memiliki ukuran visual 28dp dengan padding target sentuh dan pembesaran otomatis, disertai keterangan status tekstual pada pembaca layar.

---

## 3. Screen Reader (TalkBack & VoiceOver) Support

- **Semantics & Role Assignment**: Semua elemen interaktif kustom (seperti tombol back berbahan dasar `Box`) dideklarasikan secara eksplisit dengan `Role.Button` atau `Role.Tab`.
- **Content Descriptions**:
  - Tombol kembali: `"Kembali ke halaman sebelumnya"` / `"Kembali ke jadwal tayang"`
  - Status tiket QR: `"Kode QR reservasi tiket bioskop"`
  - Tombol pemutar trailer: `"Putar trailer resmi di YouTube"`
  - Kursi bioskop: format deskripsi semantik `"Kursi [Baris][Nomor], [Tersedia/Dipilih/Terisi], harga [Rp ...]"`.

---

## 4. Dynamic Type & 200% Font Scaling

- Seluruh layout layar menggunakan kontainer adaptif (`Column` dengan `verticalScroll`, `FlowRow` untuk chips, dan `LazyColumn` untuk daftar).
- Teks judul dan harga menggunakan `maxLines` yang fleksibel atau `TextOverflow.Ellipsis` untuk metadata pendukung, memastikan layout tidak mengalami overflow / clipping ketika pengguna mengaktifkan ukuran teks 200% di pengaturan aksesibilitas OS.
