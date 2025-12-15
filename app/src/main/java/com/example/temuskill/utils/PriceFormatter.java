package com.example.temuskill.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {

    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price);
    }

    public static String formatPriceWithUnit(double price, String unit) {
        return formatPrice(price) + "/" + unit;
    }
}