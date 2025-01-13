package com.org.smallcircle.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.R;
import com.org.smallcircle.adapter.AdapterProduct;
import com.org.smallcircle.adapter.AdapterProductHorizontal;
import com.org.smallcircle.databinding.ActivitySellerProfileBinding;
import com.org.smallcircle.model.ModelProduct;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;

public class SellerProfileActivity extends AppCompatActivity {

    private ActivitySellerProfileBinding binding;
    private static final String TAG = "SELLER_PROFILE_TAG";
    private String sellerUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerUid = getIntent().getStringExtra("sellerUid");
        Log.d(TAG, "onCreate: sellerUid: " + sellerUid);

        loadSellerDetail();
        loadProducts();

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadSellerDetail() {
        Log.d(TAG, "loadSellerDetail: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Get data
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        Long timestamp = (Long) snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimestampDate(timestamp);

                        binding.profileName.setText(name);
                        binding.memberSinceText.setText("Member since: " + formattedDate);

                        try {
                            Glide.with(SellerProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_black)
                                    .into(binding.profileImage);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProducts() {
        Log.d(TAG, "loadProducts: ");

        ArrayList<ModelProduct> productArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.orderByChild("uid").equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            try {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productArrayList.add(modelProduct);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        // Mengatur LayoutManager untuk RecyclerView
                        LinearLayoutManager layoutManager = new LinearLayoutManager(SellerProfileActivity.this, LinearLayoutManager.VERTICAL, false);
                        binding.productsRecyclerView.setLayoutManager(layoutManager);

                        // Menetapkan Adapter ke RecyclerView
                        AdapterProductHorizontal adapterProduct = new AdapterProductHorizontal(SellerProfileActivity.this, productArrayList);
                        binding.productsRecyclerView.setAdapter(adapterProduct);

                        Log.d(TAG, "Adapter set with " + productArrayList.size() + " products");

                        String productsCount = "" + productArrayList.size();
                        binding.activeListingsCount.setText(productsCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}