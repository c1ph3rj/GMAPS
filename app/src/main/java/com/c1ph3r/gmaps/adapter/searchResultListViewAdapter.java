package com.c1ph3r.gmaps.adapter;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.model.SearchResultList;

import java.util.ArrayList;

public class searchResultListViewAdapter extends ArrayAdapter<SearchResultList> {

    ArrayList<SearchResultList> listOfPlaces;

    public searchResultListViewAdapter(@NonNull Context context, ArrayList<SearchResultList> listOfPlaces) {
        super(context, R.layout.search_result_item_layout, listOfPlaces);
        this.listOfPlaces = listOfPlaces;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SearchResultList item = listOfPlaces.get(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.search_result_item_layout, parent, false);
            viewHolder.title = convertView.findViewById(R.id.searchTitle);
            viewHolder.body = convertView.findViewById(R.id.searchBody);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(item.getTitle());
        viewHolder.body.setText(item.getBody());

        return convertView;
    }

    public static class ViewHolder{
        TextView title, body;
    }
}
