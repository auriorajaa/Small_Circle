package com.org.smallcircle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import com.org.smallcircle.databinding.RowCategoryBinding;
import com.org.smallcircle.utils.RvListenerCategory;
import com.org.smallcircle.model.ModelCategory;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> {

    private RowCategoryBinding binding;
    private Context context;
    private ArrayList<ModelCategory> categoryArrayList;
    private RvListenerCategory rvListenerCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, RvListenerCategory rvListenerCategory) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.rvListenerCategory = rvListenerCategory;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory modelCategory = categoryArrayList.get(position);

        String category = modelCategory.getCategory();
        int icon = modelCategory.getIcon();


        holder.categoryIcon.setImageResource(icon);
        holder.categoryName.setText(category);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvListenerCategory.onCategoryClick(modelCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    class HolderCategory extends RecyclerView.ViewHolder {

        ShapeableImageView categoryIcon;
        TextView categoryName;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            categoryIcon = binding.categoryIcon;
            categoryName = binding.categoryName;
        }
    }
}
