package com.org.smallcircle.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.org.smallcircle.R;
import com.org.smallcircle.databinding.RowImageSliderBinding;
import com.org.smallcircle.model.ModelImageSlider;

import java.util.ArrayList;

public class AdapterImageSlider extends RecyclerView.Adapter<AdapterImageSlider.HolderImageSlider> {
    private static final String TAG = "ADAPTER_IMAGE_SLIDER_TAG";
    private Context context;
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    public AdapterImageSlider(Context context, ArrayList<ModelImageSlider> imageSliderArrayList) {
        this.context = context;
        this.imageSliderArrayList = imageSliderArrayList;
    }

    @NonNull
    @Override
    public HolderImageSlider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Buat binding baru untuk setiap item
        RowImageSliderBinding binding = RowImageSliderBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        );
        return new HolderImageSlider(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImageSlider holder, int position) {
        try {
            ModelImageSlider modelImageSlider = imageSliderArrayList.get(position);
            String imageUrl = modelImageSlider.getImageUrl();
            String imageCount = (position + 1) + "/" + imageSliderArrayList.size();

            holder.binding.imageCountText.setText(imageCount);

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.binding.imageSliderContent);

        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return imageSliderArrayList != null ? imageSliderArrayList.size() : 0;
    }

    class HolderImageSlider extends RecyclerView.ViewHolder {
        private RowImageSliderBinding binding;

        public HolderImageSlider(RowImageSliderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
