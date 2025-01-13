package com.org.smallcircle.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.org.smallcircle.R;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    public static final String PRODUCT_STATUS_AVAILABLE = "AVAILABLE";
    public static final String PRODUCT_STATUS_SOLD = "SOLD";

    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";

    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE";

    public static final String[] categories = {
            "Electronic & Computers",
            "Mobile Devices",
            "Vehicles & Parts",
            "Fashion & Beauty",
            "Home & Furniture",
            "Sports & Hobbies",
            "Toys & Baby",
            "Pet & Pet Care",
            "Health & Wellness",
            "Events & Tickets",
            "Others & Miscellaneous"
    };

    public static final String[] condition = {
            "New",
            "Like New",
            "Used - Excellent",
            "Used - Good",
            "Used - Fair",
            "Refurbished",
            "Reconditioned",
            "Open Box",
            "For Parts"
    };

    public static final int[] categoryIcons = {
            R.drawable.ic_electronic_new,
            R.drawable.ic_phone_new,
            R.drawable.ic_car_new,
            R.drawable.ic_beauty_new,
            R.drawable.ic_furniture_new,
            R.drawable.ic_hobbies_new,
            R.drawable.ic_baby_new,
            R.drawable.ic_pet_new,
            R.drawable.ic_health_new,
            R.drawable.ic_ticket_new,
            R.drawable.ic_misc_new
    };

    public static void toast(Context context, String message) {
        try {
            // Inflate custom toast layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.toast_layout, null);

            // Get the TextView from the custom layout and set the message
            TextView textView = layout.findViewById(R.id.toast_message);
            textView.setText(message);

            // Create and show the Toast
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);

            // Position the toast at the bottom with a offset from bottom
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);

            toast.show();
        } catch (Exception e) {
            // Fallback to default toast if custom toast fails
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle setting or clearing errors
    public static void setErrorState(TextInputLayout layout, String errorMessage) {
        if (errorMessage != null) {
            layout.setError(errorMessage);
            layout.setErrorEnabled(true);
        } else {
            layout.setError(null);
            layout.setErrorEnabled(false);
        }
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    public static String formatTimestampDateTime(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();

        return date;
    }

    public static void addToFavorite(Context context, String productId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're not logged in!");
        } else {
            long timestamp = Utils.getTimestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", productId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(productId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Added to favorites");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to add to favorites");
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String productId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're not logged in!");
        } else {
            long timestamp = Utils.getTimestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", productId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(productId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Remove from favorites");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to remove from favorites");
                        }
                    });
        }
    }

    public static String chatPath(String receiptUid, String yourUid) {
        String[] arrayUids = new String[]{receiptUid, yourUid};
        Arrays.sort(arrayUids);

        String chatPath = arrayUids[0] + arrayUids[1];

        return chatPath;
    }

    public static void callIntent(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void smsIntent(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void mapIntent(Context context, double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Utils.toast(context, "Google Maps app not found");
        }
    }

}
