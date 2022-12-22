package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.addresses;
import static com.c1ph3r.gmaps.MainActivity.latLng;
import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.alertTheUser;
import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.checkGPSStatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.c1ph3r.gmaps.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class MapsFragment extends Fragment {

    public GoogleMap googleMap;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap initMap) {
            // When clicked on map
            googleMap = initMap;
            // Initialize marker options
            if(checkGPSStatus(requireActivity())){
                new Handler().postDelayed(() -> setUserCurrentLocationOnMap(), 8000);
            }


        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        MaterialButton locateMe = view.findViewById(R.id.locateMeBtn);
        locateMe.setOnClickListener(onClickOfLocateMe -> setUserCurrentLocationOnMap());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void setUserCurrentLocationOnMap() {
        if(latLng != null){
            final MarkerOptions markerOptions=new MarkerOptions();
            // Set position of marker
            markerOptions.position(latLng);
            // Set title of marker
            markerOptions.title(String.valueOf(addresses.get(0)));
            // Remove all marker
            googleMap.clear();

            // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to Mountain View
                    .zoom(18)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            // Animating to zoom the marker
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // Add marker on map
            Objects.requireNonNull(googleMap.addMarker(markerOptions))
                    .setIcon(BitmapFromVector(requireActivity(), R.drawable.user_location_ic));
        }else {
            alertTheUser(requireActivity(),"Something went Wrong!", "Your Internet connection may be down or we are unable to fetch your location at the moment please try again later.");
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}