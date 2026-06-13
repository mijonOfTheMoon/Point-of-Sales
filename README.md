# Point of Sales

Aplikasi Android sederhana untuk mengelola transaksi penjualan, produk, pelanggan, kas, dan pengeluaran. Dibangun dengan Jetpack Compose dan Supabase sebagai backend.

## Fitur

- Autentikasi (login & register)
- Dashboard ringkasan penjualan
- Manajemen produk & stok
- Manajemen pelanggan
- Transaksi penjualan & riwayat transaksi
- Pencatatan kas dan pengeluaran
- Manajemen pengguna (admin)
- Profil pengguna

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- MVVM (ViewModel + Repository)
- Navigation 3
- Supabase (Auth + Postgrest)
- Kotlin Coroutines & Serialization

## Persyaratan

- Android Studio (versi terbaru)
- Android SDK 31+ (target SDK 37)
- Akun Supabase

## Setup

1. Clone repo ini.
2. Buat file `local.properties` di root project (jika belum ada) lalu tambahkan kredensial Supabase:

   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_KEY=your-anon-key
   ```

3. Sync Gradle, lalu jalankan aplikasi melalui Android Studio.

## Struktur Singkat

```
app/src/main/java/com/example/pointofsales/
├── data/         # Repository & Supabase client
├── model/        # Data class
├── navigation/   # Routing antar layar
├── ui/           # Composable screens
└── viewmodel/    # ViewModel per fitur
```
