package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.fragments.MapsFragment.destinationLatLng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.os.Bundle;

import com.c1ph3r.gmaps.R;

public class NavigationViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_view);

        if(destinationLatLng != null){
            Fragment navigationMapView = new NavigationViewMap();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, navigationMapView)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}