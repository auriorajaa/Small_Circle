package com.org.smallcircle.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.org.smallcircle.activity.ProductDetailActivity;
import com.org.smallcircle.databinding.RowProductHorizontalBinding;
import com.org.smallcircle.model.ModelProduct;
import com.org.smallcircle.utils.FilterProductHorizontal;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;

public class AdapterProductHorizontal extends RecyclerView.Adapter<AdapterProductHorizontal.HolderProduct> implements Filterable {

    private static final String TAG = "ADAPTER_PRODUCT_TAG";

    private Context context;
    public ArrayList<ModelProduct> productArrayList;
    private ArrayList<ModelProduct> filterList;
    private RowProductHorizontalBinding binding;

    private FirebaseAuth firebaseAuth;

    private FilterProductHorizontal filter;

    public AdapterProductHorizontal(Context context, ArrayList<ModelProduct> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.filterList = productArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowProductHorizontalBinding.inflate(LayoutInflater.from(context), parent, false);
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
        long timestamp = modelProduct.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);

        loadProductFirstImage(modelProduct, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite(modelProduct, holder);
        }

        holder.productTitle.setText(title);
        holder.productPrice.setText("$" + price);
        holder.productCondition.setText(condition);
//        holder.locationText.setText(address);
        holder.productCategory.setText(category);
        holder.dateText.setText(formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productId", modelProduct.getId());
                context.startActivity(intent);
            }
        });

        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = modelProduct.isFavorite();

                if (favorite) {
                    Utils.removeFromFavorite(context, modelProduct.getId());
                } else {
                    Utils.addToFavorite(context, modelProduct.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelProduct modelProduct, HolderProduct holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelProduct.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean favorite = snapshot.exists();
                        modelProduct.setFavorite(favorite);

                        if (favorite) {
                            holder.favoriteButton.setImageDrawable(context.getDrawable(R.drawable.ic_favorites_red_fill_small));
                            holder.favoriteButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.neutral_white));
                        } else {
                            holder.favoriteButton.setImageDrawable(context.getDrawable(R.drawable.ic_favorites_white_small));
                            holder.favoriteButton.setBackgroundTintList(context.getResources().getColorStateList(R.color.neutral_grey));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
            filter = new FilterProductHorizontal(this, filterList);
        }
        return filter;
    }

    class HolderProduct extends RecyclerView.ViewHolder {

        ShapeableImageView productImage;
        TextView locationText, productTitle, productPrice, productCondition, productCategory, dateText;
        ImageButton favoriteButton;

        public HolderProduct(@NonNull View itemView) {
            super(itemView);

            productImage = binding.productImage;
//            locationText = binding.locationText;
            productTitle = binding.productTitle;
            productPrice = binding.productPrice;
            productCondition = binding.productCondition;
            favoriteButton = binding.favoriteButton;
            productCategory = binding.productCategory;
            dateText = binding.productDate;
        }
    }
}
