package com.example.temuskill.utils;

public class Constants {
    // User Roles
    public static final String ROLE_PENCARI_JASA = "pencari_jasa";
    public static final String ROLE_PENYEDIA_JASA = "penyedia_jasa";

    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_CONFIRMED = "confirmed";
    public static final String ORDER_STATUS_IN_PROGRESS = "in_progress";

    // [LOGIKA BARU]
    // 1. completed = Pekerjaan selesai, tapi belum direview
    public static final String ORDER_STATUS_COMPLETED = "completed";
    // 2. reviewed = Pekerjaan selesai DAN sudah direview (Final)
    public static final String ORDER_STATUS_REVIEWED = "reviewed";

    public static final String ORDER_STATUS_CANCELLED = "cancelled";

    // Verification Status
    public static final String VERIFICATION_PENDING = "pending";
    public static final String VERIFICATION_VERIFIED = "verified";
    public static final String VERIFICATION_REJECTED = "rejected";

    // Payment Status
    public static final String PAYMENT_PENDING = "pending";
    public static final String PAYMENT_COMPLETED = "completed";
    public static final String PAYMENT_FAILED = "failed";

    // Keys
    public static final String KEY_SERVICE_ID = "SERVICE_ID";
    public static final String KEY_ORDER_ID = "ORDER_ID";
    public static final String KEY_USER_ID = "USER_ID";

    // Format
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_PHONE_LENGTH = 10;
    public static final String CURRENCY_LOCALE = "id";
    public static final String COUNTRY_LOCALE = "ID";
}