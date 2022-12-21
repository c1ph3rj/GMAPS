package com.c1ph3r.gmaps.splashScreen;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.c1ph3r.gmaps.MainActivity;
import com.c1ph3r.gmaps.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        askUserForPermission();


    }

    private void askUserForPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Welcome.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        else {
            getUserLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getUserLocationPermission();
        }else if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            new MaterialAlertDialogBuilder(this).setTitle("Location Permission Required!")
                    .setMessage("We need your location to Show your location on map so, " +
                            "Please allow your location permission in the settings app to continue.")
                    .setPositiveButton("Allow Permission", (dialogInterface, i) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(), null);
                        intent.setData(uri);
                        getPermissionResult.launch(intent);
                    }).show();
        }else{
            askUserForPermission();
        }
    }

    public ActivityResultLauncher<Intent> getPermissionResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> askUserForPermission());

    private void getUserLocationPermission() {
        if(!(isNetworkConnected())){
            new MaterialAlertDialogBuilder(this).setTitle("Internet connection required!")
                    .setMessage("Please turn on your internet connection and try again.")
                    .setPositiveButton("Ok", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))).show();
        }
        else if(!(checkGPSStatus())){
            new MaterialAlertDialogBuilder(this).setTitle("Internet connection required!")
                    .setMessage("Please turn on your internet connection and try again.")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }).show();
        }
        else {
            splashWelcomeScreen();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private boolean checkGPSStatus() {
        LocationManager locationManager;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return gps_enabled || network_enabled;
    }

    private void splashWelcomeScreen() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Welcome.this, MainActivity.class));
            finish();
        }, 5000);
    }
}