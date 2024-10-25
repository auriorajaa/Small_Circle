package com.org.smallcircle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.org.smallcircle.databinding.FragmentMyProfileBinding;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "ACCOUNT_TAG";

    int color = Color.BLUE;

    @Override
    public void onAttach(@NonNull Context context) {

        mContext = context;
        super.onAttach(context);
    }

    public MyProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyProfileBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseAuth.signOut();
                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        // Gabungkan kode dan nomor telepon
                        String phone = phoneCode + phoneNumber;

                        // Format tanggal registrasi (member since)
                        if (timestamp.equals("null")) {
                            timestamp = "0";
                        }
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));

                        // Cek dan tampilkan nilai yang sesuai
                        binding.emailText.setText(email.isEmpty() || email.equals("null") ? "Not set yet" : email);
                        binding.profileName.setText(name.isEmpty() || name.equals("null") ? "No name set" : name);
                        binding.dateOfBirthText.setText(dob.isEmpty() || dob.equals("null") ? "Not set yet" : dob);
                        binding.phoneText.setText(phone.isEmpty() || phone.equals("null") ? "Not set yet" : phone);
                        binding.memberSinceText.setText(formattedDate);

                        // Tentukan status akun berdasarkan tipe user (Email atau Phone)
                        if (userType.equals("Email")) {
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            if (isVerified) {
                                binding.accountStatusText.setText("Verified");
                                binding.accountStatusText.setTextColor(color);
                            } else {
                                binding.accountStatusText.setText("Not Verified");
                            }
                        } else {
                            binding.accountStatusText.setText("Verified");
                            binding.accountStatusText.setTextColor(color);
                        }

                        // Tampilkan foto profil
                        try {
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_black)
                                    .into(binding.profileImage);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "DatabaseError: ", error.toException());
                    }
                });
    }
}