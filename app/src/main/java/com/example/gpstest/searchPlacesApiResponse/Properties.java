
package com.example.gpstest.searchPlacesApiResponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("foursquare")
    @Expose
    private String foursquare;
    @SerializedName("landmark")
    @Expose
    private Boolean landmark;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("mapbox_id")
    @Expose
    private String mapboxId;
    @SerializedName("wikidata")
    @Expose
    private String wikidata;
    @SerializedName("accuracy")
    @Expose
    private String accuracy;

    public String getFoursquare() {
        return foursquare;
    }

    public void setFoursquare(String foursquare) {
        this.foursquare = foursquare;
    }

    public Boolean getLandmark() {
        return landmark;
    }

    public void setLandmark(Boolean landmark) {
        this.landmark = landmark;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMapboxId() {
        return mapboxId;
    }

    public void setMapboxId(String mapboxId) {
        this.mapboxId = mapboxId;
    }

    public String getWikidata() {
        return wikidata;
    }

    public void setWikidata(String wikidata) {
        this.wikidata = wikidata;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

}
