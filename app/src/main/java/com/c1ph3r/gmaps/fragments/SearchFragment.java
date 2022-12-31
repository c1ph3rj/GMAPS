package com.c1ph3r.gmaps.fragments;

import static com.c1ph3r.gmaps.MainActivity.latLng;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
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
    ChipGroup placeTypes, selectedFilters;
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
            searchLocationField = view.findViewById(R.id.SearchField);
            searchResultView = view.findViewById(R.id.SearchResultItems);
            listOfPlacesView = view.findViewById(R.id.nearByPlacesDetails);
            placeTypes = view.findViewById(R.id.TypeFilter);
            selectedFilters = view.findViewById(R.id.selectedFilter);

            ArrayList<String> selectedTypes = new ArrayList<>();
            ArrayList<String> listOfTypes = new ArrayList<>(Arrays.asList(requireActivity().getResources().getStringArray(R.array.listOfPlacesTypes)));

            for (String type : listOfTypes) {
                Chip chipBtn = new Chip(requireActivity(), null, com.google.android.material.R.style.Widget_Material3_Chip_Suggestion);
                chipBtn.setText(type);
                chipBtn.setCheckable(true);
                chipBtn.setClickable(true);
                chipBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!(buttonView.isChecked()) && selectedTypes.contains(buttonView.getText().toString())) {
                            selectedTypes.remove(buttonView.getText().toString());
                        } else {
                            selectedTypes.add(buttonView.getText().toString());
                        }
                        if (selectedTypes.size() > 0) {
                            selectedFilters.removeAllViews();
                            for (String val : selectedTypes) {
                                Chip chip = new Chip(requireContext(), null, com.google.android.material.R.style.Widget_Material3_Chip_Assist);
                                chip.setChecked(true);
                                chip.setText(val);
                                selectedFilters.addView(chip);
                                selectedFilters.setVisibility(View.VISIBLE);
                            }
                        } else {
                            selectedFilters.setVisibility(View.GONE);
                        }
                    }
                });
                placeTypes.addView(chipBtn);
            }


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
                            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
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
                listOfPlacesView.setCurrentItem(1);
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