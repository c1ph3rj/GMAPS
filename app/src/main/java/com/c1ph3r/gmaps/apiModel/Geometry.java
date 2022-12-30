package com.c1ph3r.gmaps.apiModel;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Geometry implements Serializable {

    @SerializedName("location")
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}