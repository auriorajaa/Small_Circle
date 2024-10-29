package com.org.smallcircle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    private ProgressDialog progressDialog;

    private String profileImageUrl = "";

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

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Initialize loading spinner
        binding.loadingSpinner.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.profileImage.setOnClickListener(v -> {
            // Tap akan menampilkan dialog dengan tombol close
            showProfileImageDialog(profileImageUrl, true);
        });


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

        binding.changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
            }
        });

        binding.verifyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });

        binding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Hide loading spinner
                        binding.loadingSpinner.setVisibility(View.GONE);

                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        // Set data to UI elements
                        String phone = phoneCode + phoneNumber;

                        // Check if timestamp is not null and is a valid long
                        long parsedTimestamp = 0; // Default value
                        if (timestamp != null && !timestamp.equals("null")) {
                            try {
                                parsedTimestamp = Long.parseLong(timestamp);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Invalid timestamp format", e);
                            }
                        }
                        String formattedDate = Utils.formatTimestampDate(parsedTimestamp);

                        // Menentukan panjang maksimal email
                        int maxEmailLength = 25; // Ganti dengan panjang yang diinginkan

                        // Memotong email jika terlalu panjang
                        if (email.length() > maxEmailLength) {
                            email = email.substring(0, maxEmailLength - 3) + "..."; // Menambahkan '...' di akhir
                        }

                        binding.emailText.setText(email.isEmpty() || email.equals("null") ? "Not set yet" : email);
                        binding.profileName.setText(name.isEmpty() || name.equals("null") ? "No name set" : name);
                        binding.dateOfBirthText.setText(dob.isEmpty() || dob.equals("null") ? "Not set yet" : dob);
                        binding.phoneText.setText(phone.isEmpty() || phone.equals("null") ? "Not set yet" : phone);
                        binding.memberSinceText.setText("Member since: " + formattedDate);

                        // Set account status based on account type and verification status
                        if (userType.equals("Email")) {
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();

                            if (isVerified) {
                                binding.verifyAccount.setVisibility(View.GONE);

                                binding.accountStatusText.setText("Verified");
                                binding.accountStatusText.setTextColor(Color.parseColor("#01a4ec"));
                                binding.verifiedIcon.setVisibility(View.VISIBLE);
                            } else {
                                binding.verifyAccount.setVisibility(View.VISIBLE);

                                binding.accountStatusText.setText("Not Verified");
                                binding.accountStatusText.setTextColor(Color.RED);
                                binding.verifiedIcon.setVisibility(View.GONE);
                            }
                        } else {
                            binding.verifyAccount.setVisibility(View.GONE);

                            binding.accountStatusText.setText("Verified");
                            binding.accountStatusText.setTextColor(Color.parseColor("#01a4ec"));
                            binding.verifiedIcon.setVisibility(View.VISIBLE);
                        }

                        // Show ProgressBar while loading image
                        binding.loadingSpinner.setVisibility(View.VISIBLE);

                        // Load profile image with Glide
                        Glide.with(mContext)
                                .load(profileImageUrl.isEmpty() || profileImageUrl.equals("null") ? R.drawable.ic_person_black : profileImageUrl)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(14))) // Optional: rounding corners
                                .placeholder(R.drawable.ic_person_black) // Default image while loading
                                .error(R.drawable.ic_person_black) // Error drawable if loading fails
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        binding.loadingSpinner.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        // Hide ProgressBar when image is loaded
                                        binding.loadingSpinner.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(binding.profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, "DatabaseError: ", error.toException());
                    }
                });
    }

    private void showProfileImageDialog(String imageUrl, boolean showCloseButton) {
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_profile_image);

        ImageView fullscreenImage = dialog.findViewById(R.id.fullscreen_image);
        ImageButton closeButton = dialog.findViewById(R.id.close_button);

        // Mengatur apakah tombol close ditampilkan atau tidak
        if (showCloseButton) {
            closeButton.setVisibility(View.VISIBLE);
            closeButton.setOnClickListener(v -> dialog.dismiss());
        } else {
            closeButton.setVisibility(View.GONE);
        }

        // Memuat gambar menggunakan Glide
        Glide.with(mContext)
                .load(imageUrl)
                .into(fullscreenImage);

        // Menampilkan dialog dalam mode full screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    private void verifyAccount() {
        Log.d(TAG, "verifyAccount: ");

        progressDialog.setMessage("Sending verification link to your email");
        progressDialog.show();

        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Sent");

                        progressDialog.dismiss();
                        Utils.toast(mContext, "Verification link has been sent to your email");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Utils.toast(mContext, e.getMessage());
                    }
                });
    }

}