---
title: "Memilih Tipe EC2 yang Tepat untuk General Purpose Server"
date: 2024-02-06
description: "Panduan memilih tipe instance EC2 yang tepat untuk server tujuan umum, termasuk perbandingan seri C, M, R, dan T."
categories:
  - Cloud
tags:
  - EC2
  - AWS
authors:
  - hasbi
showHero: true
heroStyle: "big"
featureimage: "img/panduan-ec2/cover12.jpg"
---

Dalam membangun infrastruktur cloud menggunakan AWS EC2, satu keputusan penting yang perlu diambil adalah memilih tipe instance yang sesuai dengan kebutuhan. Tipe instance EC2 yang dipilih akan mempengaruhi performa aplikasi yang dijalankan. Tipe instance yang ditawarkan oleh AWS EC2 sangat beragam dari instance dengan sumber daya komputasi yang besar hingga yang hemat biaya, setiap tipe memiliki karakteristik yang perlu dipertimbangkan.[^0]

<!--more-->

## *General purpose server* pakai tipe apa?

Singkat cerita, tipe yang cocok untuk *server* yang akan menjalankan aplikasi *general purpose*/tujuan umum adalah tipe C, M dan R.[^1]

Pilih tipe C jika memiliki sedikit budget, dan *instance* mementingkan sumber daya komputasi yang kuat
Pilih tipe M jika memiliki budget pas-pasan, dan menginginkan *instance* dengan CPU dan RAM yang ideal
Pilih tipe R jika memiliki banyak budget, dan *instance* ditujukan sebagai *database server*

| Tipe   | Perbandingan vCPU : RAM (GB) | Harga/vCPU/Bulan | Penggunaan Sesuai |
| :-----:| :---------------------------:  | :-------: | :-:|
| C | 1 : 2 | Rp. 500.000 | High-performance computing
| M | 1 : 4 | Rp. 600.000 | Balanced general purpose
| R | 1 : 8 | Rp. 750.000 | High-performance database

**Pertimbangan harga selengkapnya telah dirangkum pada laman situs [Perbandingan tipe AWS EC2](https://instances.vantage.sh/)**

## Apa kabar seri T?

Singkatnya, tergantung pemakaian, seri T bisa menguntungkan, namun di sisi lain bisa merugikan.

**Masalah pertama, harga**

Jika dilihat dalam daftar harga, tentu saja tipe T memiliki harga yang jauh lebih murah. Bahkan, tipe t3.large(2vCPU 8GB RAM) hanya dibanderol seharga $0.083/jam (us-east-1) atau setara dengan Rp.940.000/bulan. AWS mengklaim bahwa seri T lebih hemat sebanyak 15% dibandingkan dengan seri M.

![Perbandingan biaya T dan M](img/panduan-ec2/saving.svg)

Seri T memiliki *baseline*, yaitu batasan maksimal (5-40%) sumber daya instance tipe T dapat berjalan secara "gratis" tanpa kredit. Karena, seri T hanya bisa menggunakan seluruh sumber daya yang dimilikinya dalam jangka waktu tertentu menggunakan satuan kredit.[^2]

- Jika penggunaan CPU dibawah *baseline*, maka kredit burst bertambah
- Jika penggunaan CPU sama dengan *baseline*, maka kredit burst diam
- Jika penggunaan CPU lebih dari *baseline*, maka kredit burst berkurang

{{< accordion mode="open" separated=true >}}

{{< accordionItem title="Tabel *baseline* pada instance t4g" >}}

|Instance Size	|vCPU|	RAM |	Baseline Performance / vCPU	 |	Network Burst Bandwidth (Gbps)|	EBS Burst Bandwidth (Mbps)|
|-|-|-|-|-|-|
t4g.nano|	2|	0.5|	5%| Up to 5	|Up to 2,085
t4g.micro	|2	|1	|10%| Up to 5	|Up to 2,085
t4g.small	|2	|2|	20%| Up to 5	|Up to 2,085
t4g.medium|	2|	4	|20%| Up to 5|	Up to 2,085
t4g.large	|2	|8	|30%| Up to 5	|Up to 2,780
t4g.xlarge|	4	|16	|40%| Up to 5	|Up to 2,780
t4g.2xlarge	|8	|32	|40%| Up to 5	|Up to 2,780

{{< /accordionItem >}}

{{< accordionItem title="Tabel kredit pada instance t4g" >}}

| Instance type | CPU credits earned per hour | Maximum earned credits that can be earned in 24-hour |
|-|-|-|
t4g.nano	|6|144|
t4g.micro|12|288|
t4g.small	|24|576|
t4g.medium	|24|576|
t4g.large	|36| 864|
t4g.xlarge	|96|2304|
t4g.2xlarge	|192|4608|

{{< /accordionItem >}}

{{< /accordion >}}


Setiap 1 kredit digunakan untuk "membayar" penggunaan sumber daya dengan keterangan sebagai berikut:

- 1 kredit = 1 vCPU * 100% utilization * 1 minute.
- 1 kredit = 1 vCPU * 50% utilization * 2 minutes
- 1 kredit = 2 vCPU * 25% utilization * 2 minutes

Ketika kredit burst sudah habis, maka sumber daya yang dapat digunakan adalah sekian persen (5-40%) sesuai dengan ukuran *instance* yang disewa.[^3] Namun jika ingin menambah burst kredit, pengguna dapat beralih ke mode tanpa batas dan dikenakan biaya tambahan sesuai pemakaian, yaitu sebesar $0.05/jam/vCPU untuk T2 dan t3, $0.04/jam/vCPU untuk t4.[^4] 

> "If you are pegging the instance at 100% for a solid month, an M instance will be a better choice due to what you will pay in unlimited credits charges with a T. M instances cost a little more but aren't metered in any way so you can run them as much as you like at a predictable rate."[^5]

Contohnya seperti ini, tipe T2 varian small (1vCPU / 2GB RAM) dibanderol seharga $0.03/jam atau setara dengan Rp.340.000/bulan. Biaya ekstra yang diperlukan untuk menjalankan 100% performa T2.small selama seharian penuh adalah $1.32/hari atau setara dengan Rp.620.000. Maka, harga sebenarnya yang dikeluarkan untuk mendapatkan 100% performa T2.small untuk satu bulan penuh mencapai Rp.960.000 (Hampir setara dengan harga c7g.large(2vCPU 4GB RAM)!.

| **Tipe** | ![](img/penamaan-ec2/short-transparan.png) | t2.nano | t2.micro| t3.small |
| - | - | - | - | - |
| **CPU Credits per Hour** || 3 | 6 | 12 |
| **vCPUs** || 1 | 1 | 1 |
| **Baseline % of CPU** || 5% | 10% | 20% |
| **Cost $ per Hour** || $0.0058 | $ 0.0116 | $0.0232|
|**vCPU Hours (Baseline)**|| 1.2 | 2.4 | 4.8 |
| **vCPU Hours (Earned Credits)**| | 1.14 | 2.16 | 3.84|
| **vCPU Hours (Total Included)** || 2.34 | 4.56 | 8.64|
| **Baseline Cost $ per CPU Hour** || $0.0025 | $0.0025 | $0.0027 |
| **Baseline Cost per day** || $0.1392 | $0.2784 | $0.5568 |
| **Baseline Hours per Day** || 24 | 24 | 24 |
| **vCPU Hours to Buy** || 21.66 | 19.44 | 15.36 |
| **Unlimited Cost$ per vCPU Hour**| | $0.05 | $0.05 | $0.05 |
| **Upcharge to Run at 100%**| | $1.0830 | $0.9720 | $0.7680 |
| **Prorated Hourly Cost at 100%**| | $0.0509 | $0.0521 | $0.0552 |
| **Cost to Run at 100% All Day** || $1.2222 | $1.2504 | $1.3248 |

Melihat hasil pengujian pada tipe T yang dilakukan oleh Cast AI dengan beban kerja Kubernetes[^6] dan pengujian Coiled dengan beban kerja rekayasa data,[^7] tipe T sangat tidak cocok untuk menangani beban kerja yang banyak dan terus-menerus, karena mengakibatkan terjunnya performa dan membengkaknya biaya pemakaian.

Pada pengujian oleh beban kerja rekayasa data, Coiled menjalankan serangkaian *benchmark* Python menggunakan t3.large dan m6i.large. Kurang lebih ada 100 *benchmark* yang mencakup ilmu data umum, rekayasa data, dan beban kerja *machine learning*. Proses normalnya memerlukan waktu sekitar 150 jam. Pada akhirnya, biaya yang dikeluarkan untuk menjalankan *benchmark* pada tipe T adalah sebesar Rp.110.000, membengkak hampir dua kali lipat daripada tipe M yang hanya sebesar Rp.62.000.

![Hasil pengujian tarif burst tipe T](img/panduan-ec2/extra.svg)

**Bagaimana jika tidak membeli kredit dan hanya membayar harga dasar dari tipe T?** Yang bener aja, rugi dong! Tipe T hanya menghemat biaya sekitar 15%, sedangkan performa yang dipotong sebesar 60-95%. Gila banget.

**Masalah kedua, performa**

![Hasil pengujian durasi bencmarking tipe T](img/panduan-ec2/durasi.svg)

Pengujian yang dilakukan Coiled juga menunjukkan sumber daya yang dipakai tipe T masih kurang bagus. Benchmark yang dijalankan oleh m6i.large bisa terselesaikan dalam kurun waktu 140 jam, sedangkan t3.large memakan waktu selama 170 jam! Hal ini terlihat dari perbandingan performa disk dan network kedua instance.

![Hasil pengujian disk dan network tipe T](img/panduan-ec2/disk.svg)

m6i.large memiliki kecepatan disk dan network yang lebih cepat daripada t3.large. Hasil *benchmarking* selengkapnya dapat dilihat pada platform Grafana milik Coiled untuk [*instance* t3](https://benchmarks-grafana.oss.coiledhq.com/d/GvbFsqKVk/coiled-cluster-metrics-basic?var-datasource=Benchmarks&var-account=dask-benchmarks&var-cluster=test_array-86954b05&var-cluster-id=175900&from=1679662670000&to=1679664170000&orgId=1) dan [*instance* m6](https://aws.amazon.com/ec2/instance-types/). Processor yang digunakan m6i juga jauh lebih canggih dan cepat dibandingkan dengan processor yang dipakai pada t3.

| Instance | Processor |
|-|-|
| t3.large  | Up to 3.1 GHz Intel Xeon Scalable processor (Skylake 8175M or Cascade Lake 8259CL) |
| m6i.large | Up to 3.5 GHz 3rd Generation Intel Xeon Scalable processors (Ice Lake 8375C) |

Pada saat artikel ini ditulis, seri terbaru tipe T (t4g) menggunakan Amazon Graviton 2, padahal pada seri terbaru tipe lain (M7g,C7g) sudah memakai Amazon Graviton 3, generasi terbaru dari processor tersebut saat ini. Bahkan, seri t4g hanya menyediakan 5 Gbps network bandwidth di setiap ukuran instancenya.

Beberapa pengguna instance tipe T mengalami masalah performa khususnya pada penggunaan CPU yang mengalami throttling, yaitu ketika sumber daya CPU tidak bisa digunakan 100%. Padahal, instance tipe T yang digunakan telah disetel ke mode tak terbatas.

Para pengguna berpendapat hal ini terjadi karena adanya sistem berbagi sumber daya/*shared CPU*. Bahwa sebenarnya 5-40% dari *baseline* sumber daya CPU yang dijanjikan benar-benar diperuntukkan oleh satu penyewa, namun sisa sumber daya CPU diluar *baseline* tersebut hanya dapat dipakai tergantung seberapa banyak sumber daya sebenarnya yang sedang kosong dan yang sedang digunakan oleh penyewa lain.

> "T-instance CPUs are massively oversubscribed, meaning they sell more CPUs than what exists on the hardware - that is the whole point, that's why they have baseline utilization and bursting and why they are cheaper. I would guess they probably do have the baseline utilization reserved for you, and anything above that depends on how much CPU time the hardware has free."[^9]

> "Amazon support confirmed that if we want to have CPU guaranteed, we need to switch away from a burstable instance. They did not say how much is guaranteed for burstable. So I would say the answer is: "There is no guarantee for any CPU on a burstable instance". They seem to use "baseline utilization" as some kind of soft target, but even that seems not guaranteed."[^10]

**Jadi, siapa yang cocok menggunakan tipe T?**

Tentu saja *server* dengan beban kerja yang dilakukan relatif ringan dengan konsumsi sumber daya *instance* dibawah *baseline* dan memiliki sedikit *peak hour* (1-3 jam/hari) bisa mendapatkan keuntungan dari tipe T. Merujuk pada penjelasan sebelumnya, t4g.large dapat menyimpan kredit gratis hingga 864 kredit, dan 1 kredit dapat digunakan untuk membeli sumber daya di atas *baseline* sebanyak 100% untuk 1 menit. 

Karena t4g.large punya 2vCPU, maka dibutuhkan 2 kredit untuk menggunakan 100% kapasitasnya selama 1 menit. Dengan begitu dapat diketahui bahwa t4g.large dapat menggunakan 100% kapasitasnya dengan gratis selama 432 menit (864 kredit/2 vCPU) atau setara dengan 7 jam 12 menit.

Namun, perlu diingat bahwa 864 kredit itu dikumpulkan selama 24 jam jika instance tidak melebihi *baseline*. Setiap tipe T mendapatkan kredit gratis dalam jumlah tertentu setiap jamnya. Dalam kasus ini, t4g.large dapat mengumpulkan kredit hingga 864 kredit dengan pendapatan 36 kredit setiap jamnya. Maka t4g.large akan mengisi penuh kembali kreditnya dalam jangka waktu 24 jam (864 kredit max/36 kredit per jam).[^11]

## Intel, AMD, atau Graviton?

Amazon Web Services mengklaim bahwa prosesor Graviton lebih cocok untuk beban kerja *application servers, microservices, open-source databases,* dan *high performance computing.* Amazon Graviton juga diklaim mampu menghemat biaya sebesar 20% daripada menggunakan prosesor berbasis x86 di instance Amazon EC2, dan 60% lebih hemat energi.[^12]

Michael Larabel pada tanggal 22 Mei 2022 mempublikasikan hasil *benchmarking* ketiga processor yang terdapat pada banyak tipe instance AWS EC2, yaitu Graviton, Graviton 2, Graviton 3, Intel Xeon, dan AMD EPYC dengan sistem operasi Ubuntu 22.04 LTS dan masing-masing pada tipe instance 4xlarge. Hasil lebih lengkap dapat dibaca pada situs **[Open Benchmarking](https://openbenchmarking.org/result/2205260-PTS-GRAVITON42&shm=1&sgm=1&sts=1&swl=1&ppt=D)** atau situs **[Phoronix](https://www.phoronix.com/review/graviton3-amd-intel/9).**

&nbsp;

![Hasil benchmark AWS Graviton 3 vs Intel Xeon vs AMD EPYC](img/panduan-ec2/place.png)

&nbsp;

Dari 94 *benchmark* yang dijalankan di seluruh instance EC2 yang diuji, Graviton 3 menempati posisi pertama terbanyak dengan 43 kemenangan, disusul Intel Xeon dengan 35 kemenangan dan AMD EPYC dengan 16 kemenangan. Rata-rata geometrik dari seluruh 94 hasil *benchmark*, Graviton3 sedikit mengungguli Intel Xeon dan diikuti oleh AMD EPYC. Dengan serangkaian 94 *benchmark* yang dilakukan, Graviton3 43% lebih cepat dibandingkan instance Graviton2 berukuran sama dan 3,1x performa instance Graviton original.

&nbsp;

![Hasil benchmark AWS Graviton 3 vs Intel Xeon vs AMD EPYC](img/panduan-ec2/rank.png)

Pada laman [Compute - Amazon EC2 Instance Types](https://aws.amazon.com/ec2/instance-types/) tertera bahwa setiap vCPU pada Instance EC2 *Graviton-Based* adalah sebuah core pada AWS Graviton processor, sedangkan setiap vCPU pada Instance EC2 yang tidak berbasis Graviton adalah thread pada processor berbasis x86, dengan pengecualian beberapa tipe.

> "Each vCPU on Graviton-based Amazon EC2 instances is a core of AWS Graviton processor."

> "Each vCPU on non-Graviton-based Amazon EC2 instances is a thread of x86-based processor."[^13]

Core adalah unit pemrosesan yang ada pada CPU, sedangkan threads adalah unit pemrosesan terkecil yang berbentuk suatu rangkaian instruksi virtual. Pada sistem Amazon EC2 yang berbasis x86, satu vCPU merujuk kepada threads yang merepresentasikan setengah core. Instance yang menerapkan sistem ini mengunakan sistem *symmetric multi-processing*, yang membagi satu core menjadi dua core virtual untuk meningkatkan performa. Dengan teknologi ini, OS dapat memetakan threads kedalam vCPU yang berbeda-beda.

Kesimpulannya, Instance EC2 yang berbasis Graviton memiliki sumber daya yang lebih baik. Sebagai contoh, instance c7i yang memiliki 8vCPU sebenarnya hanya akan menggunakan 4 core CPU fisik. Tapi, instance c7g yang memiliki 8 vCPU akan benar-benar menggunakan 8 core CPU fisik. Artinya, instance c7g mendapatkan kapasitas penuh sebuah core CPU fisik untuk setiap vCPU yang membuatnya lebih *powerful!*.

[^0]: [Ragam tipe instance AWS EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html)
[^1]: [Penjelasan mendetail tipe AWS EC2](https://aws.amazon.com/ec2/instance-types/)
[^2]: [Konsep instance dengan unlimited busrt performance](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/burstable-performance-instances-unlimited-mode-concepts.html)
[^3]: [Rincian *baseline* setiap seri instance tipe T](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/burstable-credits-baseline-concepts.html#earning-CPU-credits)
[^4]: [Daftar harga burst pada instance tipe T](https://aws.amazon.com/ec2/pricing/on-demand/#T2.2Ft3.2Ft4g_Unlimited_Mode_Pricing)
[^5]: [Komentar pengguna tipe T di Reddit](https://www.reddit.com/r/aws/comments/am8if5/comment/efkd5d4/)
[^6]: [Pengujian burst vs non-burst dengan beban kerja Kubernetes](https://cast.ai/blog/burstable-vs-non-burstable-which-aws-instance-type-is-a-better-pick-for-kubernetes/)
[^7]: [Pengujain burst vs non-burst dengan beban kerja rekayasa data](https://blog.coiled.io/blog/burstable-vs-nonburstable.html)
[^8]: [*burstable charge* oleh Coiled](https://blog.coiled.io/blog/burstable-vs-nonburstable.html#id1)
[^9]: [Komentar pengguna tipe T di Stackoverflow](https://stackoverflow.com/questions/75649451/amazon-burstable-instances-e-g-t3-how-much-cpu-is-guaranteed#comment133466257_75649451)
[^10]:[Pendapat pengguna tipe T di Stackoverflow](https://stackoverflow.com/a/75874537)
[^11]:[Perincian kalkulasi kredit dan *baseline*](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/burstable-credits-baseline-concepts.html#earning-CPU-credits)
[^12]:[Keunggulan Processor Graviton](https://aws.amazon.com/ec2/graviton/)
[^13]:[Rincian processor instance EC2](https://aws.amazon.com/ec2/instance-types/)