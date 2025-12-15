package com.example.temuskill.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupExpandableItem(findViewById(R.id.item_about_1), getString(R.string.what_is_app), getString(R.string.content_about));
        setupExpandableItem(findViewById(R.id.item_about_2), getString(R.string.our_vision), getString(R.string.content_vision));
        setupExpandableItem(findViewById(R.id.item_about_3), getString(R.string.our_mission), getString(R.string.content_mission));
    }

    private void setupExpandableItem(View view, String title, String content) {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvContent = view.findViewById(R.id.tv_content);
        ImageView ivArrow = view.findViewById(R.id.iv_arrow);
        View header = view.findViewById(R.id.header_layout);

        tvTitle.setText(title);
        tvContent.setText(content);

        header.setOnClickListener(v -> {
            if (tvContent.getVisibility() == View.VISIBLE) {
                tvContent.setVisibility(View.GONE);
                ivArrow.setRotation(270);
            } else {
                tvContent.setVisibility(View.VISIBLE);
                ivArrow.setRotation(90);
            }
        });
    }
}