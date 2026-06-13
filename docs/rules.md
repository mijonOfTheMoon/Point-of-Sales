# Rules Penulisan Laporan Akhir

Acuan tetap selama mengerjakan laporan di docs/laporan/. Jangan menyimpang dari aturan ini.

## Output dan Struktur
- Satu file .txt untuk tiap subbab level X.Y. Contoh 4.1 berisi 4.1.1 sampai 4.1.8 dalam satu file.
- Folder output: docs/laporan/. Penamaan pakai prefix urutan.
- Ikuti struktur dan urutan persis seperti template laporan: Halaman Sampul, BAB I sampai BAB V beserta seluruh subbabnya.
- File output plain text. Bukan markdown. Heading ditulis sebagai teks biasa bernomor. Tabel ditulis sebagai tabel teks biasa.

## Struktur BAB
- BAB I: 1.1 Deskripsi, 1.2 Tujuan, 1.3 Tautan.
- BAB II: 2.1 Identitas, 2.2 Pembagian Modul Individu, 2.3 Fitur Bersama.
- BAB III: 3.1 Struktur Folder, 3.2 Daftar File Utama.
- BAB IV: 4.1 Hasbi, 4.2 Issadurrofiq, 4.3 Wildan. Tiap anggota punya subbab .1 sampai .8.
- BAB V: 5.1 Login dan Register, 5.2 Produk dan Inventory, 5.3 Transaksi Penjualan, 5.4 Dashboard, 5.5 Profil Pengguna, 5.6 Manajemen Pengguna.
- 5.4 memakai nama Dashboard, bukan Laporan Laba/Rugi, karena modul itu yang ada di codebase.

## Pembagian Beban
- Target keseluruhan: Hasbi sekitar 60 persen, Issadurrofiq sekitar 20 persen, Wildan sekitar 20 persen.
- Persentase dihitung dari seluruh pekerjaan, jadi modul individu di BAB IV ikut dihitung, bukan dihitung per modul.
- Beban disampaikan implisit lewat jumlah dan bobot kontribusi. Dilarang menulis angka persentase di laporan.
- Jumlah anggota tiap fitur bersama wajib bervariasi. Ada fitur yang dikerjakan tiga orang, ada yang dua orang, dan ada satu fitur yang dipegang sendiri oleh Hasbi. Jangan membuat semua fitur seolah dikerjakan rata.
- Modul individu wajib di BAB IV: Hasbi = Manajemen Kas, Issadurrofiq = Pelanggan, Wildan = Pengeluaran.

Peta kontribusi BAB V yang dikunci:
- 5.1 Login dan Register, tiga orang. Hasbi membuat AuthRepository dan AuthViewModel. Issadurrofiq membuat LoginScreen. Wildan membuat RegisterScreen.
- 5.2 Produk dan Inventory, dua orang. Hasbi membuat ProductRepository, ProductViewModel, dan ProductScreen. Wildan membuat model Product.
- 5.3 Transaksi Penjualan, tiga orang. Hasbi membuat SalesRepository, SalesViewModel, dan model Transaction. Issadurrofiq membuat SalesScreen. Wildan membuat TransactionHistoryScreen.
- 5.4 Dashboard, dua orang. Hasbi membuat DashboardRepository, DashboardViewModel, dan DashboardScreen. Issadurrofiq membuat model DashboardSummary.
- 5.5 Profil Pengguna, tiga orang. Hasbi membuat logika updateProfile di AuthRepository dan AuthViewModel. Issadurrofiq membuat ProfileScreen. Wildan membuat EditProfileSheet di dalam ProfileScreen.kt.
- 5.6 Manajemen Pengguna, satu orang. Hasbi membuat model AdminUser, UserRepository, UserViewModel, dan UserManagementScreen sendiri.

Penyesuaian tabel 2.3 mengikuti peta di atas. Kolom anggota terlibat tidak selalu ditulis Semua anggota. Untuk fitur dua orang ditulis nama yang terlibat. Untuk Manajemen Pengguna ditulis Hasbi saja.

## Gaya Bahasa
- Bilingual. Istilah teknis tetap bahasa Inggris. Contoh: function, method, repository, ViewModel, state, StateFlow, sealed interface, coroutine, suspend, composable, bottom sheet, dropdown, RPC. Jangan terjemahkan jadi fungsi, metode, kontroler.
- Diksi sehari hari tetap bahasa Indonesia. Jangan kebablasan menerjemahkan istilah teknis.
- Audiens teknis. Jangan overexplain hal yang sudah jelas.

## Ritme Kalimat. Ini Paling Penting
- Prosa wajib mengalir. Kalimat saling menyambung dan menjelaskan alur sebab akibat. Bukan daftar pernyataan pendek yang dipotong titik.
- Pakai kata penyambung dan filler supaya luwes. Contoh: akan, lalu, kemudian, lantas, tersebut, begitu, barulah, nantinya, ketika, sehingga, supaya, karena.
- Tetap hindari kalimat majemuk bertingkat yang penuh koma serta banyak dan/atau. Targetnya kalimat sedang yang mengalir, bukan kalimat super pendek dan bukan kalimat super panjang.
- Acu kembali subjek yang sudah disebut supaya antar kalimat terasa nyambung.

Contoh JELEK. Patah patah, tanpa filler, seperti daftar:
"Tampilan dipilih lewat when terhadap uiState. Saat Loading muncul CircularProgressIndicator. Saat Error muncul pesan error. Saat Success daftar kas digambar dengan LazyColumn."

Contoh BAGUS. Mengalir, ada filler, antar kalimat nyambung:
"Pemilihan tampilan akan dilakukan lewat blok when terhadap uiState yang sedang aktif. Ketika state-nya masih Loading, layar hanya menampilkan CircularProgressIndicator di tengah supaya pengguna tahu datanya sedang dimuat. Begitu state berubah menjadi Error, pesan dari state tersebut akan langsung ditampilkan memakai warna error dari theme. Barulah ketika data berhasil sampai dan state menjadi Success, seluruh daftar kas akan digambar memakai LazyColumn."

## Larangan Gaya
- Tanpa em dash.
- Tanpa tanda kurung di prosa.
- Tanpa tanda petik satu di prosa.
- Tanpa backtick di prosa.
- Tanpa frasa "efektif dan efisien".
- Tanpa pola "tidak hanya ... namun".
- Tanpa kata "sedangkan".

## Aturan Kode
- Hanya tempel potongan code yang berkaitan dengan penjelasan. Jangan tempel seluruh file.
- Code yang dihilangkan ditandai baris berisi tiga titik. Tiap baris tiga titik wajib diberi satu baris kosong di atasnya dan satu baris kosong di bawahnya supaya tidak menempel ke code.
- Prioritaskan copy paste dari codebase yang sudah ada. Jangan generate ulang.
- Jangan menambahkan comment baru pada code.
- Beri detail sespesifik mungkin. Sebut nama file, nama function, nama RPC, dan nama field yang relevan.

## Tingkat Detail. Wajib Serinci example-tutorial.txt
- Penjelasan harus rinci dan runut seperti example-tutorial.txt. Jangan berhenti di permukaan.
- Jelaskan alur data dari awal sampai akhir. Mulai dari aksi pengguna di layar, masuk ke ViewModel, lanjut ke Repository, sampai ke Supabase, lalu balik lagi mengubah state dan menggambar ulang UI.
- Untuk alur yang panjang, pakai langkah bernomor seperti example-tutorial.txt supaya urutannya jelas.
- Aturan numbering. Tiap kali penjelasan kode membahas alur eksekusi atau urutan jalannya data, tulis bagian itu sebagai daftar bernomor. Hanya bagian flow yang dibuat bernomor. Penjelasan konsep, konteks, dan alasan tetap berbentuk paragraf. Penjelasan model dan repository yang sifatnya mendeskripsikan peran function tetap paragraf karena bukan alur berurutan.

## Spesifik. Dilarang Generik. Ini Sumber Kesalahan Berulang
- Dilarang keras menulis kata Function secara generik. Selalu sebut nama function-nya. Tulis addProduct, bukan Function.
- Subjek tiap langkah flow harus nama function yang nyata, bukan ViewModel atau Function.
- Sebut parameter dan nilai yang dikirim. Jangan menulis mengirim data. Tulis data apa yang dikirim, misalnya name, price, dan stock.
- Sebut method repository yang dipanggil beserta argumennya. Tulis repository.addProduct dengan objek Product berisi name, price, dan stock.
- Sebut nama state lengkap dengan tipenya. Tulis ProductUiState.Loading dan ProductUiState.Success, bukan cuma Loading.
- Sebut nama RPC, Edge Function, tabel, kolom, dan field yang terlibat di langkah tersebut.

Contoh JELEK. Generik dan kabur:
"1. Function dijalankan di dalam viewModelScope.launch.
2. ViewModel memanggil repository untuk menjalankan operasinya ke Supabase.
3. Begitu berhasil, ViewModel memanggil loadProducts lagi sehingga nilai baru ditulis ke _uiState."

Contoh BAGUS. Menyebut nama function, parameter, dan method:
"1. addProduct menjalankan coroutine lewat viewModelScope.launch, lalu membungkus input pengguna menjadi objek Product berisi name, price, dan stock.
2. addProduct memanggil repository.addProduct yang menjalankan insert objek Product tadi ke tabel product lewat Postgrest.
3. Begitu insert berhasil, addProduct memanggil loadProducts lagi yang membaca ulang tabel product lalu menulis ProductUiState.Success berisi daftar terbaru ke _uiState."
- Jelaskan konsep di balik mekanismenya, bukan cuma menyebut nama. Contoh hal yang wajib dijelaskan: apa itu MutableStateFlow dan kenapa dibuat private, bagaimana StateFlow read only diekspos ke layar, bagaimana collectAsStateWithLifecycle membuat composable menggambar ulang ketika state berubah, kenapa memanggil loadX lagi setelah operasi tulis bisa membuat data di layar ikut berubah, apa peran viewModelScope dan Dispatchers.IO, dan bagaimana coroutine bekerja di situ.
- Jelaskan kenapa sebuah keputusan diambil, bukan cuma apa yang terjadi. Contoh kenapa saldo lewat RPC, kenapa field dibuat nullable, kenapa stok disaring.
- Tetap patuh larangan gaya dan tetap mengalir. Rinci bukan berarti patah patah.
- Perbanyak kata penyambung dan filler supaya panjang penjelasannya terasa luwes, bukan kaku.


- Bagian tautan boleh dilewati isinya. Struktur subbabnya tetap dipertahankan.
- Bagian screenshot boleh dilewati isinya. Struktur subbabnya tetap dipertahankan.

## Fakta Codebase
- Arsitektur MVVM. Alur UI Compose ke ViewModel lewat StateFlow, ViewModel ke Repository, Repository ke Supabase.
- Backend Supabase. Postgrest untuk data, Auth untuk autentikasi.
- Banyak operasi tulis lewat RPC Postgres. Contoh: process_sale, manual_kas_adjustment, activate_kas, deactivate_kas, register_customer, update_customer_profile, create_expense, cancel_expense, deactivate_product, get_dashboard_summary.
- Manajemen pengguna lewat Supabase Edge Function admin-users yang dipanggil dengan HttpURLConnection.
- Role: admin, supervisor, cashier, stocker.
- Riwayat Transaksi tidak punya model, repository, dan ViewModel sendiri. Screen ini memakai ulang milik modul Penjualan.
