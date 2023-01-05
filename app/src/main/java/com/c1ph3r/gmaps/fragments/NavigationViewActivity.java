package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.addresses;
import static com.c1ph3r.gmaps.MainActivity.getUserLocation;
import static com.c1ph3r.gmaps.MainActivity.latLng;
import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.alertTheUser;
import static com.c1ph3r.gmaps.fragments.MapsFragment.destinationLatLng;
import static com.c1ph3r.gmaps.fragments.NavigationViewMap.googleMap;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.apiModel.LatLngPoints;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NavigationViewActivity extends AppCompatActivity implements LocationSource.OnLocationChangedListener {

    ArrayList<LatLngPoints> listOfPoints;
    View navigationMap;
    TextView distanceView, durationView;
    CardView startNavigateBtn;
    LinearLayout navigationViewLayout, distanceDetailsViewLayout, bottomView;
    Handler userLocationHandler;
    Runnable getUserLocationRunnable;
    boolean isNavigationStarted = false;
    LocationManager locationSensor;
    MarkerOptions optionMarker = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_view);

        try {
            locationSensor = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationSensor.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5F, this::onLocationChanged);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if(destinationLatLng != null){
            Fragment navigationMapView = new NavigationViewMap();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, navigationMapView)
                    .commit();

            init();

            new Handler().postDelayed(() -> {
                navigationMap = navigationMapView.getView();
                try {
                    if(navigationMap != null){
                        getRoutApi();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 170);

        }
    }

    void init(){
        durationView = findViewById(R.id.durationTaken);
        distanceView = findViewById(R.id.distanceValue);
        navigationViewLayout = findViewById(R.id.navigationDetails);
        distanceDetailsViewLayout = findViewById(R.id.DistanceDetails);
        startNavigateBtn = findViewById(R.id.startNavigationBtn);
        bottomView = findViewById(R.id.bottomVal);

        navigationViewLayout.setVisibility(View.GONE);
        distanceDetailsViewLayout.setVisibility(View.VISIBLE);


        startNavigateBtn.setOnClickListener(onCLickStartNavigateBtn ->{
                distanceDetailsViewLayout.animate()
                .translationYBy(distanceDetailsViewLayout.getHeight())
                .setDuration(400)
                .alpha(0.0f);

                new Handler().postDelayed(() -> {
                    distanceDetailsViewLayout.setVisibility(View.GONE);
                    navigationViewLayout.setAlpha(0);
                    navigationViewLayout.setVisibility(View.VISIBLE);
                    navigationViewLayout.animate()
                            .translationYBy(-navigationViewLayout.getHeight())
                            .alpha(1)
                            .setDuration(400);
                    startNavigation();
                }, 400);

        });
    }

    @Override
    public void onBackPressed() {
        if(isNavigationStarted){
            isNavigationStarted = false;
            userLocationHandler.removeCallbacks(getUserLocationRunnable);
            navigationViewLayout.clearAnimation();
            distanceDetailsViewLayout.clearAnimation();
            navigationViewLayout.setAlpha(1);
            navigationViewLayout.animate()
                    .translationYBy(navigationViewLayout.getHeight())
                    .setDuration(400)
                    .alpha(0.0f);

            new Handler().postDelayed(() -> {
                navigationViewLayout.setVisibility(View.GONE);
                distanceDetailsViewLayout.setAlpha(0);
                distanceDetailsViewLayout.setVisibility(View.VISIBLE);
                distanceDetailsViewLayout.animate()
                        .translationYBy(-distanceDetailsViewLayout.getHeight())
                        .alpha(1)
                        .setDuration(400);
            }, 400);

        }else{
            finish();
        }
    }

    public void startNavigation() {
        isNavigationStarted = true;
        userLocationHandler = new Handler();
        getUserLocationRunnable = new Runnable() {
            @Override
            public void run() {
                getUserLocation(NavigationViewActivity.this);
                userLocationHandler.postDelayed(this,10000);
            }
        };

        userLocationHandler.post(getUserLocationRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    void getRoutApi()
    {
        new Thread(() -> {
            // Origin of route
            String str_origin = "origin=" + latLng.latitude + "," + latLng.longitude;

            // Destination of route
            String str_dest = "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude;

            // Sensor enabled
            String sensor = "sensor=false";
            String mode = "mode=driving";

            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

            // Output format
            String output = "json";

            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.API_KEY);

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try {
                Response response = client.newCall(request).execute();

                if(response.body() != null){
                    ResponseBody resObj = response.body();
                    JSONObject responseObj = new JSONObject(resObj.string());
                    Log.i("null", responseObj.toString());

                    if((responseObj.getJSONArray("geocoded_waypoints"))
                            .getJSONObject(0)
                            .getString("geocoder_status")
                            .equals("OK")){
                        JSONArray routes = responseObj.getJSONArray("routes");
                        for(int i = 0;i<routes.length();i++){
                            JSONObject responseRoute = routes.getJSONObject(i);

                            // getting boundaries.
                            JSONObject bounds = responseRoute.getJSONObject("bounds");
                            LatLngBounds mapBounds = setLatLongBounds(bounds);
                            // using the boundaries, cover both the lat long points inside the camera of google maps.
                            if(mapBounds != null){
                                NavigationViewActivity.this.runOnUiThread(() -> {
                                    setUserNavigationLocations();
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 120));
                                });
                            }

                            JSONArray legs = responseRoute.getJSONArray("legs");

                            for(int j = 0; j < legs.length() ; j++){
                                JSONObject distance = legs.getJSONObject(i).getJSONObject("distance");
                                JSONObject timeTaken = legs.getJSONObject(i).getJSONObject("duration");
                                JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");

                                listOfPoints = new ArrayList<>();
                                for(int k = 0; k< steps.length() ; k++){
                                    JSONObject eachStep = steps.getJSONObject(k);
                                    JSONObject distanceEachPoint = eachStep.getJSONObject("distance");
                                    JSONObject timeTakenEachPoint = eachStep.getJSONObject("duration");
                                    JSONObject startPoint = eachStep.getJSONObject("start_location");
                                    JSONObject endPoint = eachStep.getJSONObject("end_location");
                                    String instruction = eachStep.getString("html_instructions");
                                    String polyline = eachStep.getJSONObject("polyline").getString("points");

                                    LatLng eachStartPoint = new LatLng(startPoint.getDouble("lat"), startPoint.getDouble("lng"));
                                    LatLng eachEndPoint = new LatLng(endPoint.getDouble("lat"), endPoint.getDouble("lng"));
                                    String eachDistance = distanceEachPoint.getString("text");
                                    String eachDuration = timeTakenEachPoint.getString("text");
                                    ArrayList<LatLng> polyLineDecodedPoints = new ArrayList<>();
                                    try {
                                        polyLineDecodedPoints = new ArrayList<>(decodePoly(polyline));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    listOfPoints.add(new LatLngPoints(eachStartPoint, eachEndPoint, eachDistance, eachDuration, instruction, polyLineDecodedPoints));
                                }

                                PolylineOptions routeOptions = new PolylineOptions();
                                ArrayList<LatLng> polyLinePoints = new ArrayList<>();

                                for (LatLngPoints polyPoint: listOfPoints){
                                    polyLinePoints.addAll(polyPoint.getRoutePoints());
                                }


                                NavigationViewActivity.this.runOnUiThread(() -> {
                                    routeOptions.addAll(polyLinePoints)
                                            .geodesic(true)
                                            .color(Color.parseColor("#0F61A4"))
                                            .width(12);

                                    googleMap.addPolyline(routeOptions);

                                    runOnUiThread(() -> {
                                        try {
                                            String distanceVal = "Distance : " + distance.getString("text");
                                            distanceView.setText(distanceVal);
                                            String durationVal = "In : " + timeTaken.getString("text");
                                            durationView.setText(durationVal);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });

                                });

                            }
                        }
                    }

                }

            } catch (Exception e) {
                Toast.makeText(NavigationViewActivity.this, "Response Bad! please come back later.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }).start();
    }

    private LatLngBounds setLatLongBounds(JSONObject bounds) {
        try {
            JSONObject southWestObj = bounds.getJSONObject("southwest");
            JSONObject northEastObj = bounds.getJSONObject("northeast");
            LatLng southWest = new LatLng(southWestObj.getDouble("lat"), southWestObj.getDouble("lng"));
            LatLng northEast = new LatLng(northEastObj.getDouble("lat"), northEastObj.getDouble("lng"));
            return new LatLngBounds(southWest, northEast);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUserNavigationLocations() {
        try {
            if(latLng != null && destinationLatLng != null){
                final MarkerOptions currentMarker =new MarkerOptions();

                final MarkerOptions destinationMarker = new MarkerOptions();

                currentMarker.position(latLng);

                destinationMarker.position(destinationLatLng);

                currentMarker.title(String.valueOf((addresses.get(0)).getAddressLine(0)));


                // Add marker on map
                Objects.requireNonNull(googleMap.addMarker(currentMarker));
                Objects.requireNonNull(googleMap.addMarker(destinationMarker));
            }else {
                try {
                    getUserLocation(NavigationViewActivity.this);
                    setUserNavigationLocations();
                } catch (Exception e) {
                    alertTheUser(NavigationViewActivity.this,"Something went Wrong!", "Your Internet connection may be down or we are unable to fetch your location at the moment please try again later.");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        try {
            if ( googleMap == null) return;
            CameraPosition camPos = CameraPosition
                    .builder(
                            googleMap.getCameraPosition() // current Camera
                    )
                    .bearing(bearing)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(isNavigationStarted){
            try {
                optionMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                updateCameraBearing(googleMap, location.getBearing());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}