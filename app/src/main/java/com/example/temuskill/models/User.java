package com.example.temuskill.models;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class User {
    // Gunakan anotasi agar cocok dengan field "uid" di Firestore
    @PropertyName("uid")
    private String userId;

    private String namaLengkap;
    private String email;
    private String passwordHash;
    private String nomorTelepon;
    private String role;
    private long createdAt;
    private int isActive;

    // Data Profil Talent
    private String bio;
    private String keahlian;

    // Anotasi untuk field yang menggunakan underscore di Firestore
    @PropertyName("foto_profil")
    private String fotoProfil;

    private double tarif;
    private String satuanTarif;
    private List<String> galeriUrl;

    public User() {}

    public User(String namaLengkap, String email, String passwordHash, String nomorTelepon, String role) {
        this.namaLengkap = namaLengkap;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nomorTelepon = nomorTelepon;
        this.role = role;
        this.isActive = 1;
        this.createdAt = System.currentTimeMillis();
    }

    // === GETTER & SETTER DENGAN ANOTASI ===

    @PropertyName("uid")
    public String getUserId() { return userId; }

    @PropertyName("uid")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("nama_lengkap")
    public String getNamaLengkap() { return namaLengkap; }

    @PropertyName("nama_lengkap")
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    @PropertyName("foto_profil")
    public String getFotoProfilUrl() { return fotoProfil; }

    @PropertyName("foto_profil")
    public void setFotoProfilUrl(String fotoProfil) { this.fotoProfil = fotoProfil; }

    @PropertyName("nomor_telepon")
    public String getNomorTelepon() { return nomorTelepon; }

    @PropertyName("nomor_telepon")
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    @PropertyName("created_at")
    public long getCreatedAt() { return createdAt; }

    @PropertyName("created_at")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("is_active")
    public int getIsActive() { return isActive; }

    @PropertyName("is_active")
    public void setIsActive(int isActive) { this.isActive = isActive; }

    // Getter Setter Standar (CamelCase cocok otomatis)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getKeahlian() { return keahlian; }
    public void setKeahlian(String keahlian) { this.keahlian = keahlian; }

    public double getTarif() { return tarif; }
    public void setTarif(double tarif) { this.tarif = tarif; }

    public String getSatuanTarif() { return satuanTarif; }
    public void setSatuanTarif(String satuanTarif) { this.satuanTarif = satuanTarif; }

    public List<String> getGaleriUrl() { return galeriUrl; }
    public void setGaleriUrl(List<String> galeriUrl) { this.galeriUrl = galeriUrl; }
}