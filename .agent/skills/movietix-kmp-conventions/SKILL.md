---
name: movietix-ui-design
description: Aturan desain UI dan tipografi untuk project MovieTix (Compose Multiplatform). Gunakan skill ini SETIAP KALI membuat, mengubah, atau mereview UI apa pun di project ini, termasuk screen, komponen, tema, warna, font, spacing, animasi, dan layout, walaupun user tidak menyebut kata "desain". Tujuannya mencegah hasil UI generik ala "AI slop" dan menjaga konsistensi visual antar fase.
---

# MovieTix UI Design Rules

UI MovieTix harus terlihat dirancang oleh manusia yang punya selera, bukan hasil template. Skill ini berlaku untuk semua kode Compose di commonMain.

## Sumber kebenaran

1. Baca `docs/DESIGN_SYSTEM.md` SEBELUM menulis atau mengubah UI. Palet, font, scale, dan spesifikasi komponen yang ada di sana adalah acuan utama.
2. Jika `docs/DESIGN_SYSTEM.md` belum ada, berhenti dan minta dibuat dulu. Jangan mengarang desain dari nol di tengah implementasi.
3. Jika aturan di file itu bertentangan dengan skill ini, ikuti DESIGN_SYSTEM.md, lalu catat konfliknya di jawaban akhir.

## Larangan keras (ciri AI slop)

- Gradient ungu-biru default, atau gradient apa pun tanpa alasan visual yang jelas.
- Glassmorphism / blur berlebihan sebagai dekorasi.
- Semua kartu memakai shadow dan radius yang sama. Radius dan elevation harus punya hierarki dan alasan.
- Emoji atau ikon sebagai hiasan. Ikon hanya untuk fungsi (navigasi, aksi, status).
- Layout generik "hero besar + tiga kartu fitur".
- Warna hardcode (`Color(0xFF...)`) di dalam composable. Semua warna lewat theme token.
- Font default sistem tanpa pertimbangan. Font dan skala tipografi diambil dari design system.
- Teks placeholder generik seperti "Lorem ipsum", "Welcome to MovieTix", atau copywriting bergaya chatbot. Tulis microcopy yang spesifik dan singkat dalam Bahasa Indonesia.
- Animasi dekoratif (bounce, pulse, shimmer di mana-mana). Motion hanya untuk memberi makna.

## Prinsip yang harus diikuti

**Karakter visual.** Pertahankan satu arah visual yang konsisten (nuansa tiket bioskop fisik, poster film, sinema malam, sesuai DESIGN_SYSTEM.md). Setiap keputusan visual baru harus bisa dijelaskan dengan "kenapa cocok dengan arah ini".

**Token, bukan angka lepas.**
- Warna: dari `MaterialTheme.colorScheme` atau object theme custom milik project.
- Tipografi: dari `MaterialTheme.typography` atau theme custom. Dilarang memanggil `fontSize = 17.sp` langsung di screen.
- Spacing dan radius: dari scale yang didefinisikan (mis. `Spacing.md`, `Radius.sm`). Dilarang angka dp acak di screen.

**Hierarki lebih penting dari dekorasi.** Poster film adalah elemen visual utama. UI di sekitarnya harus tenang dan memberi ruang, bukan bersaing dengan poster.

**Komponen kunci harus memakai spesifikasi yang sudah ditetapkan:**
- Movie card dan poster grid
- Seat map: status available, selected, taken harus dibedakan lewat lebih dari warna saja (bentuk, ikon, pola, atau label)
- Tiket dengan potongan perforasi
- Bottom sheet pembayaran

**Semua state ditangani.** Setiap screen wajib punya tampilan untuk loading, error (dengan aksi retry), empty, dan content. Tidak boleh ada screen yang hanya menangani happy path.

**Motion bermakna.** Transisi poster ke detail, pemilihan kursi, perubahan status pembayaran. Durasi singkat, easing konsisten, dan hormati pengaturan reduced motion bila platform mendukung.

**Aksesibilitas minimum.**
- Tap target minimal 48dp.
- `contentDescription` untuk setiap gambar/ikon yang bermakna, `null` untuk yang dekoratif.
- Teks harus tetap terbaca dan layout tidak rusak pada font scale besar.
- Kontras teks memenuhi WCAG AA di light dan dark theme.

## Aturan spesifik MovieTix

- Tema ada di `core/presentation/theme/` (`MovieTixColors`, `MovieTixTypography`, `MovieTixTheme`), komponen umum di `core/presentation/components/`. Komponen yang dipakai >= 2 fitur masuk sana. Komponen satu fitur tetap di `presentation/<fitur>/`.
- Dark theme adalah tema utama. Light theme ("Matinee Mode") wajib tetap berfungsi dan lolos kontras.
- Radius poster card dan detail card memakai `radius-lg` (16dp). Jangan memakai angka radius lain.
- Elevasi lewat tonal surface layering + border 1dp, bukan drop shadow.
- Warna `cinema-text-tertiary` hanya untuk label nonaktif, bukan untuk teks hint atau teks yang harus terbaca (kontrasnya di bawah 4.5:1).
- Seat map: kotak visual 34dp dengan hit target tetap minimal 48dp; selected memakai centang, occupied memakai silang + arsiran. Batas kursi tersedia harus punya kontras minimal 3:1 terhadap latar.
- Haptic lewat `LocalHapticFeedback` di common. Jangan menambah `expect/actual` untuk UI.
- QR tiket: hitam di atas putih, ukuran 160dp, dirender dengan `qrose`.
- Di luar scope: simpan gambar tiket, tambah ke kalender, bagikan tiket. Jangan membuat UI untuk fitur ini.
- Tanpa emoji di UI, wireframe, maupun microcopy.

## Checklist sebelum selesai

Jalankan checklist ini pada setiap perubahan UI, lalu tulis hasilnya singkat di akhir jawaban:

- [ ] Tidak ada warna, ukuran font, atau dp hardcode di composable screen
- [ ] Mengikuti palet, font, dan scale dari DESIGN_SYSTEM.md
- [ ] Loading, error, empty, dan content sudah ada
- [ ] Preview (`@Preview`) tersedia untuk komponen baru, minimal light dan dark
- [ ] Tidak ada elemen dari daftar larangan di atas
- [ ] Tap target, contentDescription, dan font scaling sudah dicek
- [ ] Microcopy dalam Bahasa Indonesia, spesifik, dan tidak generik

## Bila ragu

Pilih opsi yang lebih sederhana dan lebih konsisten dengan design system, bukan opsi yang lebih "wah". Jika butuh keputusan desain baru yang belum ada di DESIGN_SYSTEM.md (warna baru, komponen baru), usulkan dulu beserta alasannya, lalu tambahkan ke dokumen tersebut sebelum dipakai di kode.
