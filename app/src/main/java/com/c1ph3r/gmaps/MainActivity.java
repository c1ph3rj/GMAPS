package com.c1ph3r.gmaps;

import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;

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
    BottomNavigationView bottomNav;

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

        getLocationOfTheUser();
        getUserLocation();

        ArrayList<Fragment> listOfFragments = new ArrayList<>();
        Fragment maps = new MapsFragment();
        listOfFragments.add(maps);
        Fragment search = new SearchFragment();
        listOfFragments.add(search);
        Fragment settings = new SettingsFragment();
        listOfFragments.add(settings);

        BottomNavAdapter bottomNavAdapter = new BottomNavAdapter(this, listOfFragments);
        container.setAdapter(bottomNavAdapter);

        container.setUserInputEnabled(false);

        bottomNav.setOnItemSelectedListener(bottomNavItem -> {
            if(bottomNavItem.getItemId() == R.id.HomePageBtn){
                container.setCurrentItem(0);
            }else if(bottomNavItem.getItemId() == R.id.SearchBtn){
                container.setCurrentItem(1);
            }else if(bottomNavItem.getItemId() == R.id.SettingsBtn){
                container.setCurrentItem(2);
            }
            return true;
        });

    }

    // Method to Fetch user Location
    @SuppressLint("MissingPermission")
    void getUserLocation() {
        CancellationTokenSource ct = new CancellationTokenSource();
        ct.getToken();
        FusedLocationProviderClient fusedLocation = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        // To fetch the current location of the user.
        fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.getToken()).addOnSuccessListener(this, location -> {
            // converting the long and lat to the address.
            Geocoder fetchAddress = new Geocoder(this, Locale.getDefault());
            try {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                // Fetching the address from the response
                addresses = fetchAddress.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                System.out.println(addresses.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(Throwable::printStackTrace);

    }// End of getUserLocation.

    void getLocationOfTheUser(){
        try {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(isNetworkConnected(MainActivity.this)){
                        if(checkGPSStatus(MainActivity.this)){
                            getUserLocation();
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