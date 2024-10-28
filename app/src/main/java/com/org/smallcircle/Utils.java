package com.org.smallcircle;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

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
}
