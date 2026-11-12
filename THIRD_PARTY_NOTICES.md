# Third-Party Notices & Attribution

This document contains licensing and attribution notices for third-party software, fonts, and data services utilized by the **MovieTix** project.

---

## 1. The Movie Database (TMDB) Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.

- **Provider**: The Movie Database (TMDB)
- **Website**: https://www.themoviedb.org
- **Terms of Use**: https://www.themoviedb.org/documentation/api/terms-of-use
- **Notice**: Film metadata, posters, backdrops, cast information, and trailers displayed within this application are retrieved via the TMDB API v3/v4.

---

## 2. Fonts & Typography

### Outfit
- **Copyright**: Copyright 2021 The Outfit Project Authors
- **License**: SIL Open Font License, Version 1.1 (OFL-1.1)
- **Source**: https://github.com/Outfitio/Outfit-Fonts

### Plus Jakarta Sans
- **Copyright**: Copyright 2020 The Plus Jakarta Sans Project Authors
- **License**: SIL Open Font License, Version 1.1 (OFL-1.1)
- **Source**: https://github.com/tokotype/PlusJakartaSans

```text
SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007

PREAMBLE
The goals of the Open Font License (OFL) are to stimulate worldwide development
of collaborative font projects, to support the font creation efforts of
academic and linguistic communities, and to provide a free and open framework
in which fonts may be shared and improved in partnership with others.

PERMISSION & CONDITIONS
Permission is hereby granted, free of charge, to any person obtaining a copy
of the Font Software, to use, study, copy, merge, embed, modify, redistribute,
and sell modified and unmodified copies of the Font Software, subject to the
conditions stated in the SIL Open Font License v1.1.
```

---

## 3. Open Source Libraries & Frameworks

The following libraries are distributed under the **Apache License, Version 2.0**:

- **JetBrains Kotlin & Compose Multiplatform**
  - Copyright 2010-2026 JetBrains s.r.o. and respective authors.
  - License: Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)

- **Ktor Client**
  - Copyright 2010-2026 JetBrains s.r.o.
  - License: Apache License 2.0

- **Koin Dependency Injection**
  - Copyright 2017-2026 Arnaud Giuliani and Koin contributors.
  - License: Apache License 2.0

- **Coil 3 (Coroutine Image Loader)**
  - Copyright 2026 Coil Contributors
  - License: Apache License 2.0

- **AndroidX Jetpack DataStore Preferences**
  - Copyright The Android Open Source Project
  - License: Apache License 2.0

- **QRose (Compose Multiplatform QR Code Generator)**
  - Copyright 2023-2026 Alexandre Gomes and contributors.
  - License: Apache License 2.0

- **Cash App Turbine**
  - Copyright 2020 Square, Inc.
  - License: Apache License 2.0

- **Square Okio**
  - Copyright 2013 Square, Inc.
  - License: Apache License 2.0

- **Kotlinx Coroutines, Serialization & DateTime**
  - Copyright 2010-2026 JetBrains s.r.o.
  - License: Apache License 2.0

---

## 4. Simulation & Educational Disclaimer

**MovieTix** is an engineering portfolio project developed for demonstration and educational purposes.

- All cinema schedules, studio types, prices, and occupied seat allocations are procedurally simulated via deterministic local algorithms (`ProceduralScheduleEngine`).
- All payment methods (Virtual Account, GoPay, OVO, Credit Card) are simulated via `DummyPaymentGateway`.
- No actual monetary transactions, banking charges, or real-world ticket bookings take place.
