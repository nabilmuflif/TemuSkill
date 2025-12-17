package com.example.temuskill.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.models.Review;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        rvReviews = findViewById(R.id.rv_my_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        loadMyReviews();
    }

    private void loadMyReviews() {
        String myUid = sessionManager.getUserId();

        // REVISI: Menambahkan addOnFailureListener untuk cek error Index
        db.collection("reviews")
                .whereEqualTo("providerId", myUid) // Ambil review UNTUK provider ini
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Review.class));
                    }

                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        rvReviews.setAdapter(new ProviderReviewAdapter(list));
                    }
                })
                .addOnFailureListener(e -> {
                    // PENTING: Log ini akan memunculkan Link Index di Logcat jika index belum dibuat
                    Log.e("FirestoreError", "Gagal ambil review: ", e);
                    Toast.makeText(MyReviewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ADAPTER KHUSUS PROVIDER (Inner Class)
    static class ProviderReviewAdapter extends RecyclerView.Adapter<ProviderReviewAdapter.ViewHolder> {
        private List<Review> reviews;
        public ProviderReviewAdapter(List<Review> reviews) { this.reviews = reviews; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_provider, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.tvName.setText("- " + (review.getUserName() != null ? review.getUserName() : "Pengguna"));
            holder.tvComment.setText(review.getComment());
            holder.ratingBar.setRating(review.getRating());
        }

        @Override
        public int getItemCount() { return reviews.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvComment;
            RatingBar ratingBar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_reviewer_name);
                tvComment = itemView.findViewById(R.id.tv_comment);
                ratingBar = itemView.findViewById(R.id.rb_rating);
            }
        }
    }
}