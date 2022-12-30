package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.latLng;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.adapter.NearByPlacesViewAdapter;
import com.c1ph3r.gmaps.apiClient.APIClient;
import com.c1ph3r.gmaps.apiClient.GoogleMapAPI;
import com.c1ph3r.gmaps.apiModel.PlacesResults;
import com.c1ph3r.gmaps.apiModel.Result;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    View view;
    TextInputEditText searchLocationField;
    ListView searchResultView;
    PlacesClient placesClient;
    ViewPager2 listOfPlacesView;
    ArrayList<NearByPlacesListItem> listOfPlaces = new ArrayList<>();
    AutocompleteSessionToken autocompleteSessionToken;
    ArrayList<Place> listOfSearchPlaces;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        // Inflate the layout for this fragment

        init();


        return view;
    }

    private void init() {
        try {
            searchLocationField = view.findViewById(R.id.SearchField);
            searchResultView = view.findViewById(R.id.SearchResultItems);
            listOfPlacesView = view.findViewById(R.id.nearByPlacesDetails);

            initPlaces();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initViewPager();
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewPager() {
        try {
            String currentLocation = latLng.latitude + "," + latLng.longitude;
            GoogleMapAPI googleMapAPI = APIClient.getClient().create(GoogleMapAPI.class);
            googleMapAPI.getNearBy(currentLocation, 10000, "Restaurant", "", getString(R.string.API_KEY)).enqueue(new Callback<PlacesResults>() {
                @Override
                public void onResponse(@NonNull Call<PlacesResults> call, @NonNull Response<PlacesResults> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        List<Result> results = response.body().getResults();
                        for(Result result: results){
                            listOfPlaces.add(new NearByPlacesListItem(result));
                        }

                        NearByPlacesViewAdapter adapter = new NearByPlacesViewAdapter(requireActivity(), listOfPlaces);
                        listOfPlacesView.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PlacesResults> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlaces() {
        Places.initialize(requireActivity(), getString(R.string.API_KEY));
        placesClient = Places.createClient(requireContext());
    }

}