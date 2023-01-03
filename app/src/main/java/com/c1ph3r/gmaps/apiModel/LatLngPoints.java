package com.c1ph3r.gmaps.apiModel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LatLngPoints {
    LatLng startPoint;
    LatLng endPoint;
    String distance;
    String duration;
    String instructions;
    ArrayList<LatLng> routePoints;

    public LatLngPoints(LatLng startPoint, LatLng endPoint, String distance, String duration, String instruction, ArrayList<LatLng> routePoints) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.duration = duration;
        this.instructions = instruction;
        this.routePoints = routePoints;
    }

    public ArrayList<LatLng> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(ArrayList<LatLng> routePoints) {
        this.routePoints = routePoints;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }
}
