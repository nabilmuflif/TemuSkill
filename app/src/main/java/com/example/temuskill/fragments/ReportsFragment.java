package com.example.temuskill.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // Import Fragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.adapters.VerificationAdapter;
import com.example.temuskill.models.ProviderProfile;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment { // Extends Fragment

    private RecyclerView rvVerification;
    private FirebaseFirestore db;
    private LineChart lineChart;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment_reports.xml
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi View menggunakan 'view.findViewById'
        rvVerification = view.findViewById(R.id.rv_verification_req);
        lineChart = view.findViewById(R.id.chart_orders_report);
        pieChart = view.findViewById(R.id.chart_category_report);

        // Gunakan getContext() untuk context
        rvVerification.setLayoutManager(new LinearLayoutManager(getContext()));

        loadVerificationRequests();
        setupLineChart();
        setupPieChart();
    }

    private void loadVerificationRequests() {
        db.collection("provider_profiles")
                .whereEqualTo("statusVerifikasi", "pending")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return; // Cek jika fragment masih aktif

                    List<ProviderProfile> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(ProviderProfile.class));
                    }
                    // Gunakan getContext() di adapter
                    rvVerification.setAdapter(new VerificationAdapter(getContext(), list));
                });
    }

    private void setupLineChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 5));
        entries.add(new Entry(1, 15));
        entries.add(new Entry(2, 10));
        entries.add(new Entry(3, 20));
        entries.add(new Entry(4, 25));
        entries.add(new Entry(5, 18));
        entries.add(new Entry(6, 35));

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#111D5E"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#111D5E"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawGridLines(false);

        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(35f, "Jasa AC/Listrik"));
        entries.add(new PieEntry(25f, "Pertanian"));
        entries.add(new PieEntry(20f, "Jahit"));
        entries.add(new PieEntry(20f, "Lainnya"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#E53935"));
        colors.add(Color.parseColor("#FFB300"));
        colors.add(Color.parseColor("#1E88E5"));
        colors.add(Color.parseColor("#43A047"));
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}