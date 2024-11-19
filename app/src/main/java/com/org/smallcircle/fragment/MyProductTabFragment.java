package com.org.smallcircle.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.adapter.AdapterProduct;
import com.org.smallcircle.adapter.AdapterProductHorizontal;
import com.org.smallcircle.databinding.FragmentMyProductBinding;
import com.org.smallcircle.databinding.FragmentMyProductTabBinding;
import com.org.smallcircle.model.ModelProduct;

import java.util.ArrayList;

public class MyProductTabFragment extends Fragment {

    private FragmentMyProductTabBinding binding;

    private static final String TAG = "MY_PRODUCT_TAG";

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productArrayList;

    private AdapterProductHorizontal adapterProduct;

    public MyProductTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;

        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyProductTabBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize search with hint instead of text
        binding.searchText.setHint("Find what you're looking for...");
        binding.searchText.setText(""); // Clear any existing text

        firebaseAuth = FirebaseAuth.getInstance();

        loadProducts();

        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    adapterProduct.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadProducts() {
        Log.d(TAG, "loadProducts: ");

        productArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
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

                        adapterProduct = new AdapterProductHorizontal(mContext, productArrayList);
                        binding.productsRecyclerView.setAdapter(adapterProduct);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}