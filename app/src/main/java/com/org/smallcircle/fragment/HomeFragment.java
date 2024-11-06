package com.org.smallcircle.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.R;
import com.org.smallcircle.activity.LocationPickerActivity;
import com.org.smallcircle.adapter.AdapterCategory;
import com.org.smallcircle.adapter.AdapterProduct;
import com.org.smallcircle.databinding.FragmentHomeBinding;
import com.org.smallcircle.model.ModelProduct;
import com.org.smallcircle.utils.RvListenerCategory;
import com.org.smallcircle.model.ModelCategory;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String TAG = "HOME_TAG";
    private static final int MAX_DISTANCE_IN_KM_TO_LOAD_PRODUCT = 10;
    private Context mContext;

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProduct adapterProduct;
    private SharedPreferences locationSp;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        Log.d(TAG, "onAttach: Context attached");
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: View created");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");

        // Set initial location text if coordinates exist
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            binding.locationText.setText("Within 10 km of " + currentAddress);
            Log.d(TAG, "onViewCreated: Current location set: " + currentAddress);
        } else {
            binding.locationText.setText("Select your location");
            Log.d(TAG, "onViewCreated: No current location found");
        }

        // Initialize search with hint instead of text
        binding.searchText.setHint("Find what you're looking for...");
        binding.searchText.setText(""); // Clear any existing text

        // Initialize product list
        productArrayList = new ArrayList<>();

        loadCategories();
        loadProducts("All Category");

        // Set up search text watcher
        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (adapterProduct != null) {
                        adapterProduct.getFilter().filter(s.toString());
                        Log.d(TAG, "onTextChanged: Filtering products with query: " + s);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: Error filtering products: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.locationSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                locationPickerActivityResult.launch(intent);
                Log.d(TAG, "onClick: Location section clicked to pick location");
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: RESULT_OK");

                        Intent data = result.getData();
                        if (data != null) {
                            Log.d(TAG, "onActivityResult: Location picked");

                            try {
                                currentLatitude = data.getDoubleExtra("latitude", 0.0);
                                currentLongitude = data.getDoubleExtra("longitude", 0.0);
                                currentAddress = data.getStringExtra("address");

                                locationSp.edit()
                                        .putFloat("CURRENT_LATITUDE", (float) currentLatitude)
                                        .putFloat("CURRENT_LONGITUDE", (float) currentLongitude)
                                        .putString("CURRENT_ADDRESS", currentAddress)
                                        .apply();

                                binding.locationText.setText(currentAddress);
                                Log.d(TAG, "onActivityResult: Updated current location: " + currentAddress);
                                loadProducts("All Category");
                            } catch (Exception e) {
                                Log.e(TAG, "onActivityResult: Error updating location: ", e);
                            }
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Location not picked");
                        Utils.toast(mContext, "Cancelled!");
                    }
                }
            }
    );

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories");
        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();

        ModelCategory modelCategoryAll = new ModelCategory("All Category", R.drawable.ic_category_new);
        categoryArrayList.add(modelCategoryAll);

        for (int i = 0; i < Utils.categories.length; i++) {
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            categoryArrayList.add(modelCategory);
        }

        AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory() {
            @Override
            public void onCategoryClick(ModelCategory modelCategory) {
                loadProducts(modelCategory.getCategory());
                Log.d(TAG, "onCategoryClick: Category clicked: " + modelCategory.getCategory());

                binding.productsHeader.setText("Near You for " + modelCategory.getCategory());
            }
        });

        binding.categoriesRecyclerView.setAdapter(adapterCategory);
        Log.d(TAG, "loadCategories: Categories loaded successfully");
    }

    private void loadProducts(String category) {
        Log.d(TAG, "loadProducts: Category: " + category);

        if (productArrayList == null) {
            productArrayList = new ArrayList<>();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                Log.d(TAG, "onDataChange: Number of products: " + snapshot.getChildrenCount());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        // Change this part to use HashMap
                        HashMap<String, Object> map = (HashMap<String, Object>) ds.getValue();
                        if (map != null) {
                            ModelProduct modelProduct = new ModelProduct();

                            modelProduct.setId(map.get("id") != null ? map.get("id").toString() : "");
                            modelProduct.setUid(map.get("uid") != null ? map.get("uid").toString() : "");
                            modelProduct.setBrand(map.get("brand") != null ? map.get("brand").toString() : "");
                            modelProduct.setCategory(map.get("category") != null ? map.get("category").toString() : "");
                            modelProduct.setCondition(map.get("condition") != null ? map.get("condition").toString() : "");
                            modelProduct.setPrice(map.get("price") != null ? map.get("price").toString() : "");
                            modelProduct.setAddress(map.get("address") != null ? map.get("address").toString() : "");
                            modelProduct.setTitle(map.get("title") != null ? map.get("title").toString() : "");
                            modelProduct.setDescription(map.get("description") != null ? map.get("description").toString() : "");
                            modelProduct.setStatus(map.get("status") != null ? map.get("status").toString() : "");
                            modelProduct.setTimestamp(map.get("timestamp") != null ? Long.parseLong(map.get("timestamp").toString()) : 0);

                            if (map.get("latitude") != null) {
                                double latitude = Double.parseDouble(map.get("latitude").toString());
                                modelProduct.setLatitude(latitude);
                            }

                            if (map.get("longitude") != null) {
                                double longitude = Double.parseDouble(map.get("longitude").toString());
                                modelProduct.setLongitude(longitude);
                            }

                            // Check if latitude and longitude are not null
                            Double productLatitude = modelProduct.getLatitude();
                            Double productLongitude = modelProduct.getLongitude();

                            if (productLatitude == null || productLongitude == null) {
                                Log.e(TAG, "loadProducts: Latitude or Longitude is null for product ID: " + modelProduct.getId());
                                continue;
                            }

                            double distance = calculateDistanceKm(productLatitude, productLongitude);
                            Log.d(TAG, "loadProducts: Product ID: " + modelProduct.getId() + ", Distance: " + distance + " km");

                            if (category.equals("All Category")) {
                                if (distance <= MAX_DISTANCE_IN_KM_TO_LOAD_PRODUCT) {
                                    productArrayList.add(modelProduct);
                                }
                            } else if (modelProduct.getCategory() != null && modelProduct.getCategory().equals(category)) {
                                if (distance <= MAX_DISTANCE_IN_KM_TO_LOAD_PRODUCT) {
                                    productArrayList.add(modelProduct);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "loadProducts: Error parsing product: ", e);
                    }
                }

                if (isAdded() && getContext() != null) {
                    Log.d(TAG, "loadProducts: Number of products to display: " + productArrayList.size());
                    adapterProduct = new AdapterProduct(mContext, productArrayList);
                    binding.productsRecyclerView.setAdapter(adapterProduct);
                    Log.d(TAG, "loadProducts: Adapter set with products");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadProducts:onCancelled: ", error.toException());
            }
        });
    }

    private double calculateDistanceKm(double adLatitude, double adLongitude) {
        Log.d(TAG, "calculateDistanceKm: currentLatitude: " + currentLatitude);
        Log.d(TAG, "calculateDistanceKm: currentLongitude: " + currentLongitude);

        Log.d(TAG, "calculateDistanceKm: adLatitude: " + adLatitude);
        Log.d(TAG, "calculateDistanceKm: adLongitude: " + adLongitude);

        try {
            Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
            startPoint.setLatitude(currentLatitude);
            startPoint.setLongitude(currentLongitude);

            Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
            endPoint.setLatitude(adLatitude);
            endPoint.setLongitude(adLongitude);

            double distanceInMeter = startPoint.distanceTo(endPoint);
            double distanceInKm = distanceInMeter / 1000;

            Log.d(TAG, "calculateDistanceKm: Distance calculated: " + distanceInKm + " km");
            return distanceInKm;
        } catch (Exception e) {
            Log.e(TAG, "calculateDistanceKm: Error calculating distance: ", e);
            return 0; // Return 0 in case of error
        }
    }
}
