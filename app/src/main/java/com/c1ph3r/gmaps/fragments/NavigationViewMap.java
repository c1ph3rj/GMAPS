package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.addresses;
import static com.c1ph3r.gmaps.MainActivity.getUserLocation;
import static com.c1ph3r.gmaps.MainActivity.latLng;
import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.alertTheUser;
import static com.c1ph3r.gmaps.fragments.MapsFragment.destinationLatLng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.apiModel.LatLngPoints;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NavigationViewMap extends Fragment {

    public GoogleMap googleMap;
    ArrayList<LatLngPoints> listOfPoints;

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
        public void onMapReady(@NonNull GoogleMap mapView) {
            googleMap = mapView;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_view_map, container, false);

        if(destinationLatLng != null){
            getRoutApi();
        }

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
                                    requireActivity().runOnUiThread(() -> {
                                        setUserCurrentLocationOnMap();
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 120));
                                    });
                                }

                                JSONArray legs = responseRoute.getJSONArray("legs");

                                for(int j = 0; j < legs.length() ; j++){
                                    JSONObject distance = legs.getJSONObject(i).getJSONObject("distance");
                                    JSONObject timeTaken = legs.getJSONObject(i).getJSONObject("duration");
                                    JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");

                                    requireActivity().runOnUiThread(() -> {
                                        try {
                                            Toast.makeText(requireContext(), distance.getString("text") +" ", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(requireContext(), timeTaken.getString("text")+ " ", Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });

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
                                        ArrayList<LatLng> polyLineDecodedPoints = new ArrayList<>(decodePoly(polyline));

                                        listOfPoints.add(new LatLngPoints(eachStartPoint, eachEndPoint, eachDistance, eachDuration, instruction, polyLineDecodedPoints));
                                    }

                                    PolylineOptions routeOptions = new PolylineOptions();
                                    ArrayList<LatLng> polyLinePoints = new ArrayList<>();

                                    for (LatLngPoints polyPoint: listOfPoints){
                                        polyLinePoints.addAll(polyPoint.getRoutePoints());
                                    }


                                    requireActivity().runOnUiThread(() -> {
                                        routeOptions.addAll(polyLinePoints)
                                                .geodesic(true)
                                                .color(Color.parseColor("#0F61A4"))
                                                .width(12);

                                        googleMap.addPolyline(routeOptions);


                                    });

                                }

                                requireActivity().startActivity(new Intent(requireActivity(), NavigationViewActivity.class));
                            }
                        }

                    }

                } catch (Exception e) {
                    Toast.makeText(requireActivity(), "Response Bad! please come back later.", Toast.LENGTH_SHORT).show();
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

    public void setUserCurrentLocationOnMap() {
        try {
            if(latLng != null){
                // Instantiating CircleOptions to draw a circle around the marker
                CircleOptions circleOptions = new CircleOptions();

                // Specifying the center of the circle
                circleOptions.center(latLng);

                // Radius of the circle
                circleOptions.radius(10000);

                // Border color of the circle
                circleOptions.strokeColor(requireActivity().getColor(R.color.primaryColor));

                // Fill color of the circle
                circleOptions.fillColor(requireActivity().getColor(R.color.darkTint));

                // Border width of the circle
                circleOptions.strokeWidth(2);


                final MarkerOptions markerOptions=new MarkerOptions();
                // Set position of marker
                markerOptions.position(latLng)
                        .flat(true);
                // Set title of marker
                markerOptions.title(String.valueOf((addresses.get(0)).getAddressLine(0)));

                // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)             // Sets the center of the map to Mountain View
                        .zoom(14)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder

                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // Add marker on map
                Objects.requireNonNull(googleMap.addMarker(markerOptions))
                        .setIcon(BitmapFromVector(requireActivity(), R.drawable.current_gps));
                // Adding the circle to the GoogleMap
                googleMap.addCircle(circleOptions);
            }else {
                try {
                    getUserLocation(requireActivity());
                    setUserCurrentLocationOnMap();
                } catch (Exception e) {
                    alertTheUser(requireActivity(),"Something went Wrong!", "Your Internet connection may be down or we are unable to fetch your location at the moment please try again later.");
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

    private static BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}