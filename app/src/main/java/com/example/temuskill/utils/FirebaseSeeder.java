package com.example.temuskill.utils;

import android.content.Context;
import android.widget.Toast;
import com.example.temuskill.models.Notification;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.Service;
import com.example.temuskill.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSeeder {

    // ID UNIK DUMMY (Agar data konsisten dan bisa di-update)
    private static final String CLIENT_ID = "client_demo_01";

    // Talent IDs
    private static final String T1_ID = "talent_ac_01";
    private static final String T2_ID = "talent_guru_02";
    private static final String T3_ID = "talent_jahit_03";
    private static final String T4_ID = "talent_montir_04";
    private static final String T5_ID = "talent_kue_05";
    private static final String T6_ID = "talent_taman_06";

    public static void seedData(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // LOGIKA PENGECEKAN:
        // Kita cek salah satu dokumen kategori ("cat_01").
        // Jika belum ada, berarti ini instalasi baru atau data belum lengkap -> Jalankan Seeder.
        db.collection("categories").document("cat_01").get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(context, "Menyiapkan Data Awal (Kategori & User)...", Toast.LENGTH_LONG).show();
                performSeed(db, context);
            }
        });
    }

    private static void performSeed(FirebaseFirestore db, Context context) {
        WriteBatch batch = db.batch();
        String pkg = context.getPackageName();

        // ==========================================
        // 1. SEEDING KATEGORI (PENTING UNTUK SPINNER)
        // ==========================================
        // Kategori ini yang nanti muncul di Dropdown saat Tambah Jasa
        createCategory(batch, db, "cat_01", "Les & Bimbingan", "Jasa guru privat akademik dan non-akademik.");
        createCategory(batch, db, "cat_02", "Menjahit & Kriya", "Jasa pembuatan dan perbaikan pakaian.");
        createCategory(batch, db, "cat_03", "Makanan & Catering", "Layanan penyediaan makanan dan kue.");
        createCategory(batch, db, "cat_04", "Servis & Perbaikan", "Teknisi elektronik, AC, dan kendaraan.");
        createCategory(batch, db, "cat_05", "Pertanian & Kebun", "Jasa perawatan taman dan tanaman.");
        createCategory(batch, db, "cat_06", "Kecantikan", "Make up artist, salon panggilan, dll.");


        // ==========================================
        // 2. CREATE CLIENT USER (PEMBELI)
        // ==========================================
        User client = new User("Budi Santoso", "budi@demo.com", "", "081234567890", "pencari_jasa");
        client.setUserId(CLIENT_ID);
        client.setIsActive(1);
        client.setFotoProfilUrl("android.resource://" + pkg + "/drawable/profile");
        batch.set(db.collection("users").document(CLIENT_ID), client);


        // ==========================================
        // 3. CREATE TALENTS (PENYEDIA JASA) + GALERI
        // ==========================================

        // --- T1: Hendra Teknik (AC) ---
        User t1 = new User("Hendra Teknik", "hendra@demo.com", "", "08120001", "penyedia_jasa");
        t1.setUserId(T1_ID); t1.setIsActive(1);
        t1.setBio("Spesialis pendingin ruangan berpengalaman 10 tahun. Jujur dan bergaransi.");
        t1.setKeahlian("Servis AC, Kulkas, Mesin Cuci");
        t1.setTarif(75000); t1.setSatuanTarif("unit");
        t1.setFotoProfilUrl("talent_1"); // Pastikan nama file ini ada di drawable, atau ganti URL

        // Galeri T1
        List<String> galeriT1 = new ArrayList<>();
        galeriT1.add("https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=600&q=80");
        galeriT1.add("https://images.unsplash.com/photo-1581094794329-cd119277ac1b?auto=format&fit=crop&w=600&q=80");
        t1.setGaleriUrl(galeriT1);

        batch.set(db.collection("users").document(T1_ID), t1);

        // --- T2: Miss Sarah (Guru) ---
        User t2 = new User("Miss Sarah", "sarah@demo.com", "", "08120002", "penyedia_jasa");
        t2.setUserId(T2_ID); t2.setIsActive(1);
        t2.setBio("Guru privat bahasa Inggris & Matematika. Metode belajar fun untuk anak SD-SMP.");
        t2.setKeahlian("Guru Les, Bahasa Inggris");
        t2.setTarif(100000); t2.setSatuanTarif("jam");
        t2.setFotoProfilUrl("talent_2");

        // Galeri T2
        List<String> galeriT2 = new ArrayList<>();
        galeriT2.add("https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=600&q=80");
        t2.setGaleriUrl(galeriT2);

        batch.set(db.collection("users").document(T2_ID), t2);

        // --- T3: Ibu Ratna (Jahit) ---
        User t3 = new User("Ibu Ratna Modiste", "ratna@demo.com", "", "08120003", "penyedia_jasa");
        t3.setUserId(T3_ID); t3.setIsActive(1);
        t3.setBio("Menerima jahitan kebaya, seragam, dan permak jeans. Hasil rapi dan cepat.");
        t3.setKeahlian("Menjahit, Kriya Kain");
        t3.setTarif(25000); t3.setSatuanTarif("potong");
        t3.setFotoProfilUrl("talent_3");

        List<String> galeriT3 = new ArrayList<>();
        galeriT3.add("https://images.unsplash.com/photo-1528642474493-227a85c42513?auto=format&fit=crop&w=600&q=80");
        t3.setGaleriUrl(galeriT3);

        batch.set(db.collection("users").document(T3_ID), t3);

        // --- T4: Mas Jono (Montir) ---
        User t4 = new User("Bengkel Mas Jono", "jono@demo.com", "", "08120004", "penyedia_jasa");
        t4.setUserId(T4_ID); t4.setIsActive(1);
        t4.setBio("Montir panggilan 24 jam area Jakarta Selatan. Ganti oli, ban bocor, mogok.");
        t4.setKeahlian("Montir Motor, Servis Kendaraan");
        t4.setTarif(50000); t4.setSatuanTarif("jasa");
        t4.setFotoProfilUrl("talent_4");

        List<String> galeriT4 = new ArrayList<>();
        galeriT4.add("https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?auto=format&fit=crop&w=600&q=80");
        t4.setGaleriUrl(galeriT4);

        batch.set(db.collection("users").document(T4_ID), t4);

        // --- T5 & T6 (Lainnya) ---
        User t5 = new User("Dapur Mama Eka", "eka@demo.com", "", "08120005", "penyedia_jasa");
        t5.setUserId(T5_ID); t5.setIsActive(1); t5.setKeahlian("Catering, Kue Basah"); t5.setTarif(30000); t5.setSatuanTarif("box");
        t5.setFotoProfilUrl("talent_5");
        batch.set(db.collection("users").document(T5_ID), t5);

        User t6 = new User("Pak Asep Taman", "asep@demo.com", "", "08120006", "penyedia_jasa");
        t6.setUserId(T6_ID); t6.setIsActive(1); t6.setKeahlian("Potong Rumput, Taman"); t6.setTarif(150000); t6.setSatuanTarif("hari");
        t6.setFotoProfilUrl("talent_6");
        batch.set(db.collection("users").document(T6_ID), t6);


        // ==========================================
        // 4. CREATE SERVICES (LAYANAN TALENT)
        // ==========================================
        // ID Service dibuat manual agar tidak duplikat
        createService(batch, db, "srv_ac_01", T1_ID, "Cuci AC Split", "Pembersihan indoor & outdoor unit");
        createService(batch, db, "srv_ac_02", T1_ID, "Isi Freon R32", "Tambah tekanan freon AC Inverter");
        createService(batch, db, "srv_ac_03", T1_ID, "Bongkar Pasang", "Pindah unit AC ke lokasi baru");

        createService(batch, db, "srv_gr_01", T2_ID, "Matematika SD", "Bimbingan PR dan persiapan ujian");
        createService(batch, db, "srv_gr_02", T2_ID, "Conversation English", "Lancar bicara Inggris dalam 3 bulan");

        createService(batch, db, "srv_jh_01", T3_ID, "Permak Jeans", "Potong panjang, kecilkan pinggang");


        // ==========================================
        // 5. CREATE ORDERS (RIWAYAT PESANAN)
        // ==========================================
        // Order 1: Selesai (Hendra)
        Order o1 = new Order(CLIENT_ID, T1_ID, "Senin, 10 Okt 2024", 75000);
        o1.setOrderId("ord_01");
        o1.setServiceName("Cuci AC Split");
        o1.setProviderName("Hendra Teknik");
        o1.setProviderId(T1_ID);
        o1.setServiceId(T1_ID);
        o1.setStatusPesanan("completed");
        o1.setReviewed(true);
        batch.set(db.collection("orders").document("ord_01"), o1);

        // Order 2: Aktif (Sarah)
        Order o2 = new Order(CLIENT_ID, T2_ID, "Besok, 16:00 WIB", 100000);
        o2.setOrderId("ord_02");
        o2.setServiceName("Conversation English");
        o2.setProviderName("Miss Sarah");
        o2.setProviderId(T2_ID);
        o2.setServiceId(T2_ID);
        o2.setStatusPesanan("confirmed");
        batch.set(db.collection("orders").document("ord_02"), o2);


        // ==========================================
        // 6. CREATE REVIEWS (ULASAN)
        // ==========================================
        createReview(batch, db, "rev_01", T1_ID, "ord_01", "Sangat dingin! Kerjanya rapi dan bersih.", 5, "Budi Santoso");
        createReview(batch, db, "rev_02", T1_ID, "ord_dummy_1", "Teknisi datang tepat waktu. Mantap.", 5, "Siti Aminah");
        createReview(batch, db, "rev_03", T1_ID, "ord_dummy_2", "Harga bersahabat, pelayanan oke.", 4, "Joko");
        createReview(batch, db, "rev_04", T2_ID, "ord_dummy_3", "Anak saya jadi suka bahasa Inggris. Makasih Miss!", 5, "Mama Dini");


        // ==========================================
        // 7. CREATE NOTIFICATIONS
        // ==========================================
        Notification n1 = new Notification(CLIENT_ID, "Pesanan Selesai", "Layanan Cuci AC Split telah selesai. Yuk beri ulasan!");
        batch.set(db.collection("notifications").document("notif_01"), n1);

        Notification n2 = new Notification(CLIENT_ID, "Pesanan Diterima", "Miss Sarah menerima pesanan Anda untuk besok.");
        batch.set(db.collection("notifications").document("notif_02"), n2);

        Notification n3 = new Notification(CLIENT_ID, "Selamat Datang", "Selamat bergabung di TemuSkill! Temukan jasa terbaik di sini.");
        batch.set(db.collection("notifications").document("notif_03"), n3);


        // EKSEKUSI BATCH (SIMPAN SEMUA DATA)
        batch.commit()
                .addOnSuccessListener(a -> Toast.makeText(context, "✅ Data Dummy & Kategori Berhasil!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "❌ Gagal Seeding: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- HELPER METHODS ---

    // 1. Helper Buat Kategori
    private static void createCategory(WriteBatch batch, FirebaseFirestore db, String id, String name, String desc) {
        Map<String, Object> data = new HashMap<>();
        data.put("categoryId", id);
        data.put("name", name);
        data.put("description", desc);
        batch.set(db.collection("categories").document(id), data);
    }

    // 2. Helper Buat Service
    private static void createService(WriteBatch batch, FirebaseFirestore db, String serviceId, String providerId, String name, String details) {
        Service s = new Service(providerId, name, details);
        s.setServiceId(serviceId);
        s.setOrderCount(10 + (int)(Math.random() * 50));
        batch.set(db.collection("services").document(serviceId), s);
    }

    // 3. Helper Buat Review
    private static void createReview(WriteBatch batch, FirebaseFirestore db, String reviewId, String providerId, String orderId, String comment, float rating, String reviewerName) {
        Map<String, Object> review = new HashMap<>();
        review.put("providerId", providerId);
        review.put("orderId", orderId);
        review.put("userId", "user_random_" + System.currentTimeMillis());
        review.put("userName", reviewerName);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", System.currentTimeMillis());
        batch.set(db.collection("reviews").document(reviewId), review);
    }
}