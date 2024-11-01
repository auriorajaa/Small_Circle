package com.org.smallcircle.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.org.smallcircle.R;
import com.org.smallcircle.databinding.UploadImageCardBinding;
import com.org.smallcircle.model.ModelImagePicked;

import java.util.ArrayList;

public class AdapterImagePicked extends RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked> {

    private UploadImageCardBinding binding;
    private static final String TAG = "IMAGE_TAG";

    private Context context;
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    public AdapterImagePicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList) {
        this.context = context;
        this.imagePickedArrayList = imagePickedArrayList;
    }

    @NonNull
    @Override
    public HolderImagePicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = UploadImageCardBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderImagePicked(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImagePicked holder, @SuppressLint("RecyclerView") int position) {

        ModelImagePicked model = imagePickedArrayList.get(position);

        Uri imageUri = model.getImageUri();
        Log.d(TAG, "onBindViewHolder: imageUri: " + imageUri);

        try {
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.uploadedImageView);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        holder.deleteUploadedImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickedArrayList.remove(model);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePickedArrayList.size();
    }

    class HolderImagePicked extends RecyclerView.ViewHolder{

        ImageView uploadedImageView;
        TextView deleteUploadedImageButton;

        public HolderImagePicked(@NonNull View itemView) {
            super(itemView);

            uploadedImageView = binding.uploadedImageView;
            deleteUploadedImageButton = binding.deleteUploadedImageButton;
        }
    }
}
