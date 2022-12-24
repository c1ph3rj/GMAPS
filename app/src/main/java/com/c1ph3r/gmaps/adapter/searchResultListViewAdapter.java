package com.c1ph3r.gmaps.adapter;

import android.content.Context;
import android.location.Address;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class searchResultListViewAdapter extends ArrayAdapter<Address> {

    public searchResultListViewAdapter(@NonNull Context context, int resource, ArrayList<Address> foundAddresses) {
        super(context, resource, foundAddresses);
    }

    public static class ViewHolder{

    }
}
