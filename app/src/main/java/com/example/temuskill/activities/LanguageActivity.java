package com.example.temuskill.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        RadioButton rbIndo = findViewById(R.id.rb_indo);
        RadioButton rbEng = findViewById(R.id.rb_english);
        Button btnApply = findViewById(R.id.btn_apply);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Cek bahasa saat ini
        String currentLang = getResources().getConfiguration().locale.getLanguage();
        if (currentLang.equals("in") || currentLang.equals("id")) rbIndo.setChecked(true);
        else rbEng.setChecked(true);

        btnApply.setOnClickListener(v -> {
            String langCode = rbIndo.isChecked() ? "in" : "en";
            setLocale(langCode);
        });
    }

    private void setLocale(String langCode) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(langCode));
        res.updateConfiguration(conf, dm);

        // Restart App agar bahasa berubah
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}