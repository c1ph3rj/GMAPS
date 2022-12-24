package com.c1ph3r.gmaps.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.c1ph3r.gmaps.MainActivity;
import com.c1ph3r.gmaps.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    View view;
    TextInputEditText searchLocationField;
    ArrayList<String> searchResultList;
    ArrayList<LatLng> searchResultLatLng;
    ListView searchResultView;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        // Inflate the layout for this fragment

        init();
        searchMenu();

        return view;
    }

    private void init() {
        try {
            searchLocationField = view.findViewById(R.id.SearchField);
            searchResultView = view.findViewById(R.id.SearchResultItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchMenu() {
        searchLocationField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    searchResultList = new ArrayList<>();
                    searchResultLatLng = new ArrayList<>();
                    if (editable != null && !editable.toString().isEmpty()) {
                        // Create a Geocoder object
                        Geocoder geocoder = new Geocoder(requireContext());

                        // Perform the search
                        List<Address> addresses = geocoder.getFromLocationName(editable.toString(), 10);
                        if (addresses.size() > 0) {
                            // Get the first result
                            Address address = addresses.get(0);
                            for (Address addressInSearch : addresses) {
                                searchResultList.add(addressInSearch.getAddressLine(0));
                                searchResultLatLng.add(new LatLng(addressInSearch.getLatitude(), addressInSearch.getLongitude()));
                            }
                            initListView();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initListView() {
        try {
            ArrayAdapter<String> searchResultAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, searchResultList);
            searchResultView.setAdapter(searchResultAdapter);
            searchResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MainActivity.bottomNav.setSelectedItemId(R.id.HomePageBtn);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}