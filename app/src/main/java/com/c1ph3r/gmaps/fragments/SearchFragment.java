package com.c1ph3r.gmaps.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.c1ph3r.gmaps.R;

public class SearchFragment extends Fragment {
    View view;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        // Inflate the layout for this fragment

        searchMenu();

        return view;
    }

    private void searchMenu() {
        if(view != null){

        }
    }

}