package com.c1ph3r.gmaps;

import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;

import com.c1ph3r.gmaps.adapter.BottomNavAdapter;
import com.c1ph3r.gmaps.databinding.ActivityMainBinding;
import com.c1ph3r.gmaps.fragments.MapsFragment;
import com.c1ph3r.gmaps.fragments.SearchFragment;
import com.c1ph3r.gmaps.fragments.SettingsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    ActivityMainBinding BindMain;

    public static List<Address> addresses;
    public static LatLng latLng;
    public static BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BindMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(BindMain.getRoot());

        init();
    }

    void init(){
        bottomNav = BindMain.BottomNavigation;
        ViewPager2 container = BindMain.ViewPager;

        try {
            getLocationOfTheUser();
            getUserLocation(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Fragment> listOfFragments = new ArrayList<>();
        Fragment maps = new MapsFragment();
        listOfFragments.add(maps);
        Fragment search = new SearchFragment();
        Log.i("MapFragment", String.valueOf(search.getId()));
        listOfFragments.add(search);
        Fragment settings = new SettingsFragment();
        listOfFragments.add(settings);

        BottomNavAdapter bottomNavAdapter = new BottomNavAdapter(this, listOfFragments);
        container.setAdapter(bottomNavAdapter);

        container.setUserInputEnabled(false);
        container.setClipToPadding(false);
        container.setClipChildren(false);
        container.setOffscreenPageLimit(3);
        container.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        // Code for carousel view animation in viewpager2.
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float val = 1 - Math.abs(position);
            page.setScaleY(0.85f + val * 0.15f);
        });

        // Adding carousel view animation to the viewpager2.
        container.setPageTransformer(compositePageTransformer);

        bottomNav.setOnItemSelectedListener(bottomNavItem -> {
            try {
                if(bottomNavItem.getItemId() == R.id.HomePageBtn){
                    container.setCurrentItem(0);
                }else if(bottomNavItem.getItemId() == R.id.SearchBtn){
                    container.setCurrentItem(1);
                }else if(bottomNavItem.getItemId() == R.id.SettingsBtn){
                    container.setCurrentItem(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });

    }

    // Method to Fetch user Location
    @SuppressLint("MissingPermission")
    public static void getUserLocation(Activity context) {
        try {
            CancellationTokenSource ct = new CancellationTokenSource();
            ct.getToken();
            FusedLocationProviderClient fusedLocation = LocationServices.getFusedLocationProviderClient(context);
            // To fetch the current location of the user.
            fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.getToken()).addOnSuccessListener(context, location -> {
                // converting the long and lat to the address.
                Geocoder fetchAddress = new Geocoder(context, Locale.getDefault());
                try {
                    if(location != null){
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        // Fetching the address from the response
                        addresses = fetchAddress.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.i("Location : ", addresses.get(0).toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(Throwable::printStackTrace);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }// End of getUserLocation.

    void getLocationOfTheUser(){
        try {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(isNetworkConnected(MainActivity.this)){
                        if(checkGPSStatus(MainActivity.this)){
                            getUserLocation(MainActivity.this);
                        }else {
                            alertTheUser(MainActivity.this, "Location disabled!", "Your GPS service disabled. Please turn on your GPS to fetch your location.");
                        }
                    }else {
                        alertTheUser(MainActivity.this, "Internet connection required!", "Your internet connection disabled. Please enable your internet connection to continue.");
                    }
                    handler.postDelayed(this, 120000); //now is every 2 minutes
                }
            }, 120000); //Every 120000 ms (2 minutes)
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}