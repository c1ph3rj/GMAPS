package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.bottomNav;
import static com.c1ph3r.gmaps.MainActivity.imm;
import static com.c1ph3r.gmaps.MainActivity.latLng;
import static com.c1ph3r.gmaps.MainActivity.screenHeight;

import android.graphics.Rect;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.adapter.NearByPlacesViewAdapter;
import com.c1ph3r.gmaps.apiClient.APIClient;
import com.c1ph3r.gmaps.apiClient.GoogleMapAPI;
import com.c1ph3r.gmaps.apiModel.PlacesResults;
import com.c1ph3r.gmaps.apiModel.Result;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    View view;
    PlacesClient placesClient;
    ViewPager2 listOfPlacesView;


    ArrayList<NearByPlacesListItem> listOfPlaces = new ArrayList<>();
    private final Handler sliderHandler = new Handler();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        // Inflate the layout for this fragment

        try {
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;
    }

    private void init() {
        try {
            listOfPlacesView = view.findViewById(R.id.nearByPlacesDetails);

            initPlaces();



            new Handler().postDelayed(this::initViewPager, 3000);
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
                    try {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            List<Result> results = response.body().getResults();
                            for (Result result : results) {
                                listOfPlaces.add(new NearByPlacesListItem(result));
                            }
                            NearByPlacesViewAdapter adapter = new NearByPlacesViewAdapter(requireActivity(), listOfPlaces);
                            listOfPlacesView.setAdapter(adapter);


                            listOfPlacesView.setClipToPadding(false);
                            listOfPlacesView.setClipChildren(false);
                            listOfPlacesView.setOffscreenPageLimit(3);
                            listOfPlacesView.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                            compositePageTransformer.addTransformer(new MarginPageTransformer(5));
                            compositePageTransformer.addTransformer((page, position) -> {
                                float r = 1 - Math.abs(position);
                                page.setScaleY(0.85f + r * 0.15f);
                            });

                            listOfPlacesView.setPageTransformer(compositePageTransformer);

                            listOfPlacesView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                                @Override
                                public void onPageSelected(int position) {
                                    super.onPageSelected(position);
                                    sliderHandler.removeCallbacks(sliderRunnable);
                                    sliderHandler.postDelayed(sliderRunnable, 2000); // slide duration 2 seconds
                                }
                            });

                        } else {
                            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (listOfPlacesView.getCurrentItem() == listOfPlaces.size() - 1) {
                listOfPlacesView.setCurrentItem(0);
            } else {
                listOfPlacesView.setCurrentItem(listOfPlacesView.getCurrentItem() + 1);
            }
        }
    };

    private void initPlaces() {
        try {
            Places.initialize(requireActivity(), getString(R.string.API_KEY));
            placesClient = Places.createClient(requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 2000);
    }
}