
package com.example.gpstest.searchPlacesApiResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Places {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("query")
    @Expose
    private List<String> query;
    @SerializedName("features")
    @Expose
    private List<Feature> features;
    @SerializedName("attribution")
    @Expose
    private String attribution;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getQuery() {
        return query;
    }

    public void setQuery(List<String> query) {
        this.query = query;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

}
