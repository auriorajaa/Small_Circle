package com.org.smallcircle.utils;

import android.widget.Filter;

import com.org.smallcircle.adapter.AdapterChats;
import com.org.smallcircle.model.ModelChats;

import java.util.ArrayList;

public class FilterChats extends Filter {

    private AdapterChats adapter;
    private ArrayList<ModelChats> filterList;

    public FilterChats(AdapterChats adapter, ArrayList<ModelChats> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelChats> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getName().toUpperCase().contains(constraint)) {
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return null;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.chatsArrayList = (ArrayList<ModelChats>) results.values;

        adapter.notifyDataSetChanged();
    }
}
