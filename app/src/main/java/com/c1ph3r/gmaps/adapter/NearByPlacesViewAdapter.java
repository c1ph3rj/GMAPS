package com.c1ph3r.gmaps.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.c1ph3r.gmaps.fragments.NearByPlacesListItem;

import java.util.ArrayList;

public class NearByPlacesViewAdapter extends FragmentStateAdapter {
    ArrayList<NearByPlacesListItem> listOfPlaces;

    public NearByPlacesViewAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<NearByPlacesListItem> listOfPlaces) {
        super(fragmentActivity);
        this.listOfPlaces = listOfPlaces;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return listOfPlaces.get(position);
    }

    @Override
    public int getItemCount() {
        return listOfPlaces.size();
    }
}
