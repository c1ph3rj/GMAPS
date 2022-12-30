package com.c1ph3r.gmaps.fragments;

import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.c1ph3r.gmaps.R;
import com.c1ph3r.gmaps.apiModel.Result;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class NearByPlacesListItem extends Fragment {

    Result result;
    public NearByPlacesListItem(){}

    public NearByPlacesListItem(Result detailOfPlace){
        this.result = detailOfPlace;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_near_by_places_list_item, container, false);

        if(view != null){
            TextView Title, Body;
            RatingBar ratings;
            ImageView imageView;

            Title = view.findViewById(R.id.nearByPlaceTitle);
            Body = view.findViewById(R.id.nearByPlaceDescription);
            imageView = view.findViewById(R.id.listOfImages);
            ratings = view.findViewById(R.id.nearByPlaceRatings);

            Title.setText(result.getName());
            Body.setText(result.getVicinity());
            if(result.getRating() != null){
                ratings.setRating((Objects.requireNonNull(result.getRating())).floatValue());
            }else{
                ratings.setVisibility(View.GONE);
            }

            try {
                if(result.getPhotos() != null){
                    String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=600&maxheight=400&photo_reference=" + result.getPhotos().get(0).getPhotoReference() +"&sensor=false&key="+ getString(R.string.API_KEY);
                    Picasso.get()
                            .load(url)
                            .resize(imageView.getWidth(), 600)
                            .into(imageView);
                }else{
                    throw new Exception();
                }
            } catch (Exception e) {
                imageView.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.no_images_found));
                e.printStackTrace();
            }


        }

        return view;
    }
}