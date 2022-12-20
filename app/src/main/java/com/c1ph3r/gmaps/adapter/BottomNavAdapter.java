package com.c1ph3r.gmaps.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class BottomNavAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> listOfFragments;

    public BottomNavAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Fragment> listOfFragments) {
        super(fragmentActivity);
        this.listOfFragments = listOfFragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return listOfFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return listOfFragments.size();
    }

}
