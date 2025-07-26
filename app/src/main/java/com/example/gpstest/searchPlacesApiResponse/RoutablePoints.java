
package com.example.gpstest.searchPlacesApiResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RoutablePoints {

    @SerializedName("points")
    @Expose
    private Object points;

    public Object getPoints() {
        return points;
    }

    public void setPoints(Object points) {
        this.points = points;
    }

}
