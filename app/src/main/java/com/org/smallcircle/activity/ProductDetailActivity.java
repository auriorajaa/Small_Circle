package com.org.smallcircle.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.R;
import com.org.smallcircle.adapter.AdapterImageSlider;
import com.org.smallcircle.databinding.ActivityProductDetailBinding;
import com.org.smallcircle.model.ModelImageSlider;
import com.org.smallcircle.model.ModelProduct;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;

    private static final String TAG = "PRODUCT_DETAIL_TAG";
    private FirebaseAuth firebaseAuth;

    private String productId = "";

    private double productLatitude = 0;
    private double productLongitude = 0;

    private String sellerUid = null;
    private String sellerPhone = "";

    private boolean favorite = false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnEdit.setVisibility(View.GONE);
        binding.btnDelete.setVisibility(View.GONE);
        binding.btnChat.setVisibility(View.GONE);
        binding.btnCall.setVisibility(View.GONE);
        binding.btnMap.setVisibility(View.GONE);
        binding.bottomActBtnLayout.setVisibility(View.GONE);

        productId = getIntent().getStringExtra("productId");

        Log.d(TAG, "onCreate: productId: " + productId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }

        loadProductDetail();
        loadProductImages();

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ProductDetailActivity.this, R.style.AlertDialogTheme);

                // Set dialog title, message, and buttons
                materialAlertDialogBuilder.setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete this product?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProduct();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProduct();
            }
        });

        binding.btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite) {
                    Utils.removeFromFavorite(ProductDetailActivity.this, productId);
                } else {
                    Utils.addToFavorite(ProductDetailActivity.this, productId);
                }
            }
        });

        binding.sellerInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailActivity.this, SellerProfileActivity.class);
                intent.putExtra("sellerUid", sellerUid);
                startActivity(intent);
            }
        });

        binding.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);
            }
        });

        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callIntent(ProductDetailActivity.this, sellerPhone);
            }
        });

        binding.btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.smsIntent(ProductDetailActivity.this, sellerPhone);
            }
        });

        binding.btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.mapIntent(ProductDetailActivity.this, productLatitude, productLongitude);
            }
        });
    }

    private void editProduct() {
        Log.d(TAG, "editProduct: ");

        PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(this, R.style.PopupMenuWhiteBackground), binding.btnEdit);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Mark as sold");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == 0) {
                    Intent intent = new Intent(ProductDetailActivity.this, AddProductActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("productId", productId);

                    startActivity(intent);
                } else if (itemId == 1) {
                    showMarkAsSoldDialog();
                }

                return true;
            }
        });
    }

    private void showMarkAsSoldDialog() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);

        alertDialogBuilder.setTitle("Mark as Sold")
                .setMessage("Are you sure you want to mark this product as sold?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Sold clicked...");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", Utils.PRODUCT_STATUS_SOLD);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
                        ref.child(productId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Marked as sold!");
                                        Utils.toast(ProductDetailActivity.this, "Marked as sold!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: ", e);
                                        Utils.toast(ProductDetailActivity.this, e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel clicked...");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadProductDetail() {
        Log.d(TAG, "loadProductDetail: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelProduct modelProduct = snapshot.getValue(ModelProduct.class);

                            sellerUid = modelProduct.getUid();
                            String title = modelProduct.getTitle();
                            String category = modelProduct.getCategory();
                            String description = modelProduct.getDescription();
                            String price = modelProduct.getPrice();
                            String status = modelProduct.getStatus();
                            String address = modelProduct.getAddress();
                            String condition = modelProduct.getCondition();
                            productLatitude = modelProduct.getLatitude();
                            productLongitude = modelProduct.getLongitude();
                            long timestamp = modelProduct.getTimestamp();

                            String formattedDate = Utils.formatTimestampDate(timestamp);

                            if (sellerUid.equals(firebaseAuth.getUid())) {
                                binding.btnEdit.setVisibility(View.VISIBLE);
                                binding.btnDelete.setVisibility(View.VISIBLE);

                                binding.ownProductText.setVisibility(View.VISIBLE);

                                binding.bottomActBtnLayout.setVisibility(View.VISIBLE);
                                binding.callSmsMapsLayout.setVisibility(View.GONE);
                                binding.btnChat.setVisibility(View.GONE);
                                binding.btnCall.setVisibility(View.GONE);
                                binding.btnSms.setVisibility(View.GONE);
                                binding.btnMap.setVisibility(View.GONE);
                                binding.sellerInfoCard.setVisibility(View.GONE);

                            } else {
                                binding.btnEdit.setVisibility(View.GONE);
                                binding.btnDelete.setVisibility(View.GONE);

                                binding.ownProductText.setVisibility(View.GONE);

                                binding.bottomActBtnLayout.setVisibility(View.VISIBLE);
                                binding.btnChat.setVisibility(View.VISIBLE);
                                binding.btnCall.setVisibility(View.VISIBLE);
                                binding.btnSms.setVisibility(View.VISIBLE);
                                binding.btnMap.setVisibility(View.VISIBLE);
                                binding.sellerInfoCard.setVisibility(View.VISIBLE);

                            }

                            binding.txtTitle.setText(title);
                            binding.txtCategory.setText(category);
                            binding.txtDescription.setText(description);
                            binding.txtPrice.setText("$" + price);
                            binding.txtDate.setText("Posted " + formattedDate);
                            binding.txtCondition.setText("Condition: " + condition);
                            binding.txtAddress.setText(address);

                            Log.d(TAG, "onDataChange: title: " + title);
                            Log.d(TAG, "onDataChange: description: " + description);
                            Log.d(TAG, "onDataChange: price: " + price);
                            Log.d(TAG, "onDataChange: status: " + status);
                            Log.d(TAG, "onDataChange: address: " + address);
                            Log.d(TAG, "onDataChange: condition: " + condition);
                            Log.d(TAG, "onDataChange: posted: " + formattedDate);

                            loadSellerDetail();

                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSellerDetail() {
        Log.d(TAG, "loadSellerDetail: sellerUid: " + sellerUid);

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(sellerUid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                String phoneCode = "" + snapshot.child("phoneCode").getValue(String.class);
                                String phoneNumber = "" + snapshot.child("phoneNumber").getValue(String.class);
                                String name = "" + snapshot.child("name").getValue(String.class);
                                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue(String.class);
                                long timestamp = snapshot.child("timestamp").getValue(Long.class);

                                String formattedDate = Utils.formatTimestampDate(timestamp);

                                sellerPhone = phoneCode + "" + phoneNumber;

                                binding.txtSellerName.setText(name);
                                binding.txtMemberSince.setText("Member since " + formattedDate);

                                try {
                                    Glide.with(ProductDetailActivity.this)
                                            .load(profileImageUrl)
                                            .placeholder(R.drawable.ic_person_black)
                                            .into(binding.imgSellerProfile);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error loading image: ", e);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing seller data: ", e);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "loadSellerDetail: ", e);
        }
    }

    private void checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: ");
                        favorite = snapshot.exists();

                        if (favorite) {
                            binding.btnFavorite.setImageResource(R.drawable.ic_favorites_red_fill);
                            binding.btnFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent_red)));
                        } else {
                            binding.btnFavorite.setImageResource(R.drawable.ic_favorites_white);
                            binding.btnFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.neutral_black)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductImages() {
        Log.d(TAG, "loadProductImages: Starting to load images for productId: " + productId);

        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "loadProductImages: ProductId is null or empty");
            return;
        }

        if (imageSliderArrayList == null) {
            imageSliderArrayList = new ArrayList<>();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(productId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            Log.d(TAG, "onDataChange: Number of images found: " + snapshot.getChildrenCount());
                            imageSliderArrayList.clear();

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                try {
                                    ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                                    if (modelImageSlider != null) {
                                        imageSliderArrayList.add(modelImageSlider);
                                        Log.d(TAG, "Added image with URL: " + modelImageSlider.getImageUrl());
                                    } else {
                                        Log.e(TAG, "Failed to parse image data for child: " + ds.getKey());
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing individual image: ", e);
                                }
                            }

                            if (imageSliderArrayList.isEmpty()) {
                                Log.d(TAG, "No images found for this product");
                                // Mungkin tampilkan placeholder atau pesan "No Images"
                                return;
                            }

                            if (!isFinishing() && !isDestroyed()) {
                                AdapterImageSlider adapterImageSlider = new AdapterImageSlider(
                                        ProductDetailActivity.this,
                                        imageSliderArrayList
                                );
                                binding.viewPagerImages.setAdapter(adapterImageSlider);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error in onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
    }

    private void deleteProduct() {
        Log.d(TAG, "deleteProduct: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(productId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");

                        Utils.toast(ProductDetailActivity.this, "Product deleted successfully!");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(ProductDetailActivity.this, e.getMessage());
                    }
                });
    }
}