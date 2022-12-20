package com.c1ph3r.gmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.c1ph3r.gmaps.adapter.BottomNavAdapter;
import com.c1ph3r.gmaps.databinding.ActivityMainBinding;
import com.c1ph3r.gmaps.fragments.MapsFragment;
import com.c1ph3r.gmaps.fragments.SearchFragment;
import com.c1ph3r.gmaps.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding BindMain;

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

        ArrayList<Fragment> listOfFragments = new ArrayList<>();
        Fragment maps = new MapsFragment();
        listOfFragments.add(maps);
        Fragment search = new SearchFragment();
        listOfFragments.add(search);
        Fragment settings = new SettingsFragment();
        listOfFragments.add(settings);

        BottomNavAdapter bottomNavAdapter = new BottomNavAdapter(this, listOfFragments);
        container.setAdapter(bottomNavAdapter);

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
}