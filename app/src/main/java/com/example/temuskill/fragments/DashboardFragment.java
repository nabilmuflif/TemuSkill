package com.example.temuskill.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.temuskill.R;
import com.example.temuskill.activities.IntroActivity;
import com.example.temuskill.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvUser, tvSeeker, tvProvider, tvCat;
    private LineChart lineChart;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        tvUser = view.findViewById(R.id.tv_total_user);
        tvSeeker = view.findViewById(R.id.tv_total_seeker);
        tvProvider = view.findViewById(R.id.tv_total_provider);
        tvCat = view.findViewById(R.id.tv_total_categories);
        lineChart = view.findViewById(R.id.chart_orders);

        view.findViewById(R.id.btn_logout_header).setOnClickListener(v -> performLogout());

        loadStats();
        setupChart();
    }

    private void loadStats() {
        db.collection("users").count().get(AggregateSource.SERVER).addOnSuccessListener(t -> tvUser.setText(String.valueOf(t.getCount())));
        db.collection("users").whereEqualTo("role", "penyedia_jasa").count().get(AggregateSource.SERVER).addOnSuccessListener(t -> tvProvider.setText(String.valueOf(t.getCount())));
        db.collection("users").whereEqualTo("role", "pencari_jasa").count().get(AggregateSource.SERVER).addOnSuccessListener(t -> tvSeeker.setText(String.valueOf(t.getCount())));
        db.collection("categories").count().get(AggregateSource.SERVER).addOnSuccessListener(t -> tvCat.setText(String.valueOf(t.getCount())));
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 10)); entries.add(new Entry(1, 25));
        entries.add(new Entry(2, 15)); entries.add(new Entry(3, 40));

        LineDataSet set = new LineDataSet(entries, "Order");
        set.setColor(Color.parseColor("#111D5E"));
        set.setDrawFilled(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(set));
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void performLogout() {
        SessionManager sessionManager = new SessionManager(getContext());
        sessionManager.logout();
        SharedPreferences introPref = requireContext().getSharedPreferences("TemuSkillPref", Context.MODE_PRIVATE);
        introPref.edit().putBoolean("isIntroShown", false).apply();
        Intent intent = new Intent(getContext(), IntroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}