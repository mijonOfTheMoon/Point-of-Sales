# Rules Gaya Penulisan General

Acuan gaya penulisan dokumentasi teknis. Berlaku umum, lepas dari struktur dokumen, pembagian tugas, maupun project tertentu. Aturan ini soal cara menulis, bukan soal format dokumen.

## Gaya Bahasa
- Bilingual. Istilah teknis tetap bahasa Inggris. Contoh: function, method, class, service, handler, state, cache, request, response, thread, query. Jangan terjemahkan jadi fungsi, metode, kelas, kontroler.
- Diksi sehari hari tetap bahasa Indonesia. Jangan kebablasan menerjemahkan istilah teknis, dan jangan pula meng-Inggriskan kata biasa.
- Audiens teknis. Jangan overexplain hal yang sudah jelas bagi pembaca yang paham.

## Ritme Kalimat. Ini Paling Penting
- Prosa wajib mengalir. Kalimat saling menyambung dan menjelaskan alur sebab akibat. Bukan daftar pernyataan pendek yang dipotong titik.
- Pakai kata penyambung dan filler supaya luwes. Contoh: akan, lalu, kemudian, lantas, tersebut, begitu, barulah, nantinya, ketika, sehingga, supaya, karena.
- Tetap hindari kalimat majemuk bertingkat yang penuh koma serta banyak dan/atau. Targetnya kalimat sedang yang mengalir, bukan kalimat super pendek dan bukan kalimat super panjang.
- Acu kembali subjek yang sudah disebut supaya antar kalimat terasa nyambung.

Contoh JELEK. Patah patah, tanpa filler, seperti daftar:
"Request masuk lewat handler. Handler memvalidasi input. Validasi gagal mengembalikan error. Validasi lolos memanggil service."

Contoh BAGUS. Mengalir, ada filler, antar kalimat nyambung:
"Tiap request akan lebih dulu masuk lewat handler yang memegang validasi input. Ketika validasinya gagal, handler langsung mengembalikan error supaya proses tidak berlanjut. Barulah ketika validasinya lolos, handler meneruskan datanya ke service untuk diproses lebih jauh."

## Larangan Gaya
- Tanpa em dash.
- Tanpa tanda kurung di prosa.
- Tanpa tanda petik satu di prosa.
- Tanpa backtick di prosa.
- Tanpa frasa "efektif dan efisien".
- Tanpa pola "tidak hanya ... namun".
- Tanpa kata "sedangkan".

## Tingkat Detail. Wajib Rinci dan Runut
- Penjelasan harus rinci dan runut. Jangan berhenti di permukaan.
- Jelaskan alur data dari awal sampai akhir. Mulai dari pemicunya, masuk ke lapisan logika, lanjut ke lapisan data atau layanan luar, lalu balik lagi mengubah state dan hasil akhir yang terlihat.
- Jelaskan konsep di balik mekanismenya, bukan cuma menyebut nama. Bila menyebut sebuah konstruksi, jelaskan apa perannya dan kenapa dipakai.
- Jelaskan kenapa sebuah keputusan diambil, bukan cuma apa yang terjadi. Contoh kenapa sebuah nilai dibuat opsional, kenapa data disaring lebih dulu, kenapa operasi dijalankan di lapisan tertentu.
- Rinci bukan berarti patah patah. Tetap patuh ritme kalimat yang mengalir dan perbanyak kata penyambung.

## Numbering untuk Flow
- Tiap kali penjelasan membahas alur eksekusi atau urutan jalannya data, tulis bagian itu sebagai daftar bernomor.
- Hanya bagian flow yang dibuat bernomor. Penjelasan konsep, konteks, dan alasan tetap berbentuk paragraf.
- Penjelasan yang sifatnya mendeskripsikan peran sesuatu, bukan urutan langkah, tetap paragraf.

## Spesifik. Dilarang Generik. Ini Sumber Kesalahan Berulang
- Dilarang menulis subjek generik seperti "Function" atau "Sistem". Selalu sebut nama yang nyata.
- Subjek tiap langkah flow harus nama yang konkret, misalnya nama function atau komponen yang benar benar menjalankan langkah itu.
- Sebut parameter dan nilai yang dikirim. Jangan menulis mengirim data. Tulis data apa yang dikirim.
- Sebut method atau endpoint yang dipanggil beserta argumennya.
- Sebut nama state lengkap dengan tipenya, bukan cuma nama singkatnya.
- Sebut nama resource yang terlibat di langkah tersebut, misalnya nama tabel, kolom, field, atau layanan.

Contoh JELEK. Generik dan kabur:
"1. Function dijalankan.
2. Sistem memanggil service untuk memproses data.
3. Begitu berhasil, data baru disimpan ke state."

Contoh BAGUS. Menyebut nama, parameter, dan tujuan konkret:
"1. saveOrder mulai berjalan dengan membawa items dan customerId dari form.
2. saveOrder memanggil orderService.create yang mengirim items dan customerId ke endpoint POST /orders.
3. Begitu response sukses diterima, saveOrder memanggil refreshOrders yang membaca ulang daftar lalu menulis OrderState.Success berisi data terbaru ke state."

## Aturan Kode
- Hanya tempel potongan code yang berkaitan dengan penjelasan. Jangan tempel seluruh file.
- Code yang dihilangkan ditandai baris berisi tiga titik. Tiap baris tiga titik wajib diberi satu baris kosong di atasnya dan satu baris kosong di bawahnya supaya tidak menempel ke code.
- Prioritaskan copy paste dari sumber yang sudah ada. Jangan generate ulang.
- Jangan menambahkan comment baru pada code.
- Beri detail sespesifik mungkin saat merujuk code. Sebut nama file, nama function, dan nama identifier yang relevan.
