package com.org.smallcircle.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.R;
import com.org.smallcircle.databinding.RowProductBinding;
import com.org.smallcircle.model.ModelProduct;
import com.org.smallcircle.utils.FilterProduct;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProduct> implements Filterable {

    private static final String TAG = "ADAPTER_PRODUCT_TAG";

    private Context context;
    public ArrayList<ModelProduct> productArrayList;
    private ArrayList<ModelProduct> filterList;
    private RowProductBinding binding;

    private FirebaseAuth firebaseAuth;

    private FilterProduct filter;

    public AdapterProduct(Context context, ArrayList<ModelProduct> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.filterList = productArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowProductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderProduct(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProduct holder, int position) {
        ModelProduct modelProduct = productArrayList.get(position);

        String title = modelProduct.getTitle();
        String price = modelProduct.getPrice();
        String condition = modelProduct.getCondition();
        String address = modelProduct.getAddress();
        String category = modelProduct.getCategory();

        loadProductFirstImage(modelProduct, holder);

        holder.productTitle.setText(title);
        holder.productPrice.setText("$" + price);
        holder.productCondition.setText(condition);
        holder.locationText.setText(address);
        holder.productCategory.setText(category);
    }

    private void loadProductFirstImage(ModelProduct modelProduct, HolderProduct holder) {
        Log.d(TAG, "loadProductFirstImage: ");

        String productId = modelProduct.getId();
        Log.d(TAG, "loadProductFirstImage: Product ID: " + productId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Product");
        reference.child(productId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: Number of images: " + snapshot.getChildrenCount());

                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String imageUrl = "" + ds.child("imageUrl").getValue();
                                Log.d(TAG, "onDataChange: imageUrl: " + imageUrl);

                                try {
                                    Glide.with(context)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_image_gray)
                                            .into(holder.productImage);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error loading image: ", e);
                                }
                            }
                        } else {
                            Log.d(TAG, "onDataChange: No images found for product ID: " + productId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProduct extends RecyclerView.ViewHolder {

        ShapeableImageView productImage;
        TextView locationText, productTitle, productPrice, productCondition, productCategory;
        ImageButton favoriteButton;

        public HolderProduct(@NonNull View itemView) {
            super(itemView);

            productImage = binding.productImage;
            locationText = binding.locationText;
            productTitle = binding.productTitle;
            productPrice = binding.productPrice;
            productCondition = binding.productCondition;
            favoriteButton = binding.favoriteButton;
            productCategory = binding.productCategory;
        }
    }
}
