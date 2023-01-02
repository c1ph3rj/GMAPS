package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.addresses;
import static com.c1ph3r.gmaps.MainActivity.getUserLocation;
import static com.c1ph3r.gmaps.MainActivity.latLng;
import static com.c1ph3r.gmaps.common.IsEverythingFineCheck.alertTheUser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.model.Marker;
import com.c1ph3r.gmaps.MainActivity;
import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.adapter.searchResultListViewAdapter;
import com.c1ph3r.gmaps.apiClient.GoogleMapAPI;
import com.c1ph3r.gmaps.apiModel.Geometry;
import com.c1ph3r.gmaps.apiModel.Result;
import com.c1ph3r.gmaps.common.DirectionsJSONParser;
import com.c1ph3r.gmaps.model.SearchResultList;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MapsFragment extends Fragment {

    public static GoogleMap googleMap;
    TextInputLayout searchLayout;
    TextInputEditText searchField;
    CardView navigateBtn, locateMe;
    LinearLayout searchResultLayout;
    ArrayList<String> searchResultList;
    ArrayList<SearchResultList> listOfSearchPlaces;
    ListView searchResultView;
    public static int destinationPoint =0;
    public static LatLng destinationLatLng;
    PlacesClient placesClient;
    AutocompleteSessionToken autocompleteSessionToken;

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
            // To align compass.
            initMap.setPadding(20, 140, 20, 20);

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        try {
            init(view);

            basicFunctions();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void basicFunctions() {
        try {
            locateMe.setOnClickListener(onClickOfLocateMe -> setUserCurrentLocationOnMap());
            searchField.setOnFocusChangeListener((view1, b) -> {
                if(b){
                    MainActivity.bottomNav.setVisibility(View.GONE);
                    searchResultLayout.setVisibility(View.VISIBLE);
                    searchResultView.setVisibility(View.VISIBLE);
                    TranslateAnimation animate = new TranslateAnimation(
                            0,                 // fromXDelta
                            0,                 // toXDelta
                            searchResultLayout.getHeight(),  // fromYDelta
                            0);                // toYDelta
                    animate.setDuration(500);
                    animate.setFillAfter(true);
                    searchResultLayout.startAnimation(animate);
                    searchResultView.setVisibility(View.VISIBLE);
                    locateMe.setVisibility(View.GONE);
                    navigateBtn.setVisibility(View.GONE);
                }else{
                    MainActivity.bottomNav.setVisibility(View.VISIBLE);
                    TranslateAnimation animate = new TranslateAnimation(
                            0,        // fromXDelta
                            0,                 // toXDelta
                            0,                 // fromYDelta
                            searchResultLayout.getHeight()); // toYDelta
                    animate.setDuration(500);
                    animate.setFillAfter(true);
                    searchResultLayout.startAnimation(animate);
                    new Handler().postDelayed(() -> searchResultLayout.setVisibility(View.GONE), 500);
                    navigateBtn.setVisibility(View.VISIBLE);
                    searchResultView.setVisibility(View.GONE);
                    locateMe.setVisibility(View.VISIBLE);
                }
            });

            searchLayout.setEndIconOnClickListener(onClickClearSearchField -> {
                searchField.setText("");
                listOfSearchPlaces = new ArrayList<>();
                searchResultView.setAdapter(new searchResultListViewAdapter(requireActivity(), listOfSearchPlaces));
            });

            navigateBtn.setOnClickListener(v -> new Thread(() -> {
                if(destinationPoint == 1){
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

//                            OkHttpClient client = new OkHttpClient().newBuilder()
//                                    .build();
//                            MediaType mediaType = MediaType.parse("text/plain");
//                            RequestBody body = RequestBody.create(mediaType, "");
//                            Request request = new Request.Builder()
//                                    .url(url)
//                                    .get()
//                                    .build();
//                            try {
//                                Response response = client.newCall(request).execute();
//
//                                if(response.body() != null){
//                                    String resp = response.body().toString();
//                                    ResponseBody obj = response.body();
//                                    JSONObject ob = new JSONObject(obj.string());
//                                    Log.i("null", ob.toString());
//
//                                    ParserTask parserTask = new ParserTask();
//                                    parserTask.execute(obj.string());
//
//                                }
//
//
//
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }

                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);


                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUserCurrentLocationOnMap();
                        }
                    });
                }
            }).start());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
            Log.d("data", data);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    private static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            Log.d("result", result.toString());
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null){
                googleMap.addPolyline(lineOptions);
            }
        }
    }


    void init(View view){
        try {
            locateMe = view.findViewById(R.id.locateMeBtn);
            navigateBtn = view.findViewById(R.id.Navigate);
            searchField = view.findViewById(R.id.SearchField);
            searchLayout = view.findViewById(R.id.search_field_layout);
            searchResultLayout = view.findViewById(R.id.searchResultLayout);
            searchResultView = view.findViewById(R.id.SearchResultItems);
            initPlaces();

            searchMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlaces() {
        Places.initialize(requireActivity(), getString(R.string.API_KEY));
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

    public static void setMarkerOnNewLocation(Result result, Context requireContext) {
        try {
            if(result != null){
                MarkerOptions markerOptions=new MarkerOptions();
                // Set position of marker
                LatLng newPosition = new LatLng((result.getGeometry()).getLocation().getLat(), (result.getGeometry()).getLocation().getLng());
                markerOptions.position(Objects.requireNonNull((newPosition)));
                // Set title of marker
                markerOptions.title(result.getVicinity());
                // Remove all marker
                googleMap.clear();

                destinationPoint = 1;
                destinationLatLng = newPosition;

                // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(newPosition)        // Sets the center of the map to Mountain View
                        .zoom(15)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder

                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // Add marker on map
                Objects.requireNonNull(googleMap.addMarker(markerOptions))
                        .setIcon(BitmapFromVector(requireContext, R.drawable.user_location_ic));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setMarkerOnNewLocation(Place newPosition, Context context){
        try {
            if(newPosition != null){

                MarkerOptions markerOptions=new MarkerOptions();
                // Set position of marker
                markerOptions.position(Objects.requireNonNull(newPosition.getLatLng()));
                // Set title of marker
                markerOptions.title(newPosition.getAddress());
                // Remove all marker
                googleMap.clear();

                destinationPoint = 1;
                destinationLatLng = newPosition.getLatLng();

                // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(newPosition.getLatLng())      // Sets the center of the map to Mountain View
                        .zoom(15)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder

                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // Add marker on map
                Objects.requireNonNull(googleMap.addMarker(markerOptions))
                        .setIcon(BitmapFromVector(context, R.drawable.user_location_ic));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void searchMenu() {
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(!Objects.requireNonNull(searchField.getText()).toString().trim().isEmpty()){
                        searchResultList = new ArrayList<>();
                        autocompleteSessionToken = AutocompleteSessionToken.newInstance();
                        placesClient = Places.createClient(requireActivity());
                        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                                .setQuery(Objects.requireNonNull(searchField.getText()).toString().trim())
                                .setCountry((addresses.get(0)).getCountryCode())
                                .setSessionToken(autocompleteSessionToken)
                                .setOrigin(latLng)
                                .build();
                        placesClient.findAutocompletePredictions(request)
                                .addOnSuccessListener(response -> {
                                    listOfSearchPlaces = new ArrayList<>();
                                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                                        listOfSearchPlaces.add(new SearchResultList(prediction.getPrimaryText(null).toString()
                                        ,prediction.getSecondaryText(null).toString(), prediction.getPlaceId()));
                                        searchResultList.add(prediction.getPrimaryText(null).toString());
                                    }
                                    if(searchResultList.size() != 0){
                                        initListView();
                                    }
                                }).addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(requireActivity(), "Please check your internet connections!", Toast.LENGTH_SHORT).show();
                                });


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initListView() {
        try {
            searchResultListViewAdapter adapter = new searchResultListViewAdapter(requireActivity(), listOfSearchPlaces);
            searchResultView.setAdapter(adapter);
            searchResultView.setOnItemClickListener((parent, view, position, id) -> {
                requireActivity().onBackPressed();
                List<Place.Field> placesFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                FetchPlaceRequest request = FetchPlaceRequest.builder(
                        listOfSearchPlaces.get(position).getPlaceId(), placesFields
                ).build();
                placesClient.fetchPlace(request).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    setMarkerOnNewLocation(place, requireActivity());
                }).addOnFailureListener(e ->
                        Toast.makeText(requireActivity(), "Please Check your internet connection.", Toast.LENGTH_SHORT).show());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}