
package com.example.gpstest.searchPlacesApiResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Feature {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("place_type")
    @Expose
    private List<String> placeType;
    @SerializedName("relevance")
    @Expose
    private Double relevance;
    @SerializedName("properties")
    @Expose
    private Properties properties;
    @SerializedName("routable_points")
    @Expose
    private RoutablePoints routablePoints;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("place_name")
    @Expose
    private String placeName;
    @SerializedName("center")
    @Expose
    private List<Double> center;
    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("context")
    @Expose
    private List<Context> context;
    @SerializedName("matching_text")
    @Expose
    private String matchingText;
    @SerializedName("matching_place_name")
    @Expose
    private String matchingPlaceName;
    @SerializedName("bbox")
    @Expose
    private List<Double> bbox;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPlaceType() {
        return placeType;
    }

    public void setPlaceType(List<String> placeType) {
        this.placeType = placeType;
    }

    public Double getRelevance() {
        return relevance;
    }

    public void setRelevance(Double relevance) {
        this.relevance = relevance;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public RoutablePoints getRoutablePoints() {
        return routablePoints;
    }

    public void setRoutablePoints(RoutablePoints routablePoints) {
        this.routablePoints = routablePoints;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public List<Double> getCenter() {
        return center;
    }

    public void setCenter(List<Double> center) {
        this.center = center;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public List<Context> getContext() {
        return context;
    }

    public void setContext(List<Context> context) {
        this.context = context;
    }

    public String getMatchingText() {
        return matchingText;
    }

    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
    }

    public String getMatchingPlaceName() {
        return matchingPlaceName;
    }

    public void setMatchingPlaceName(String matchingPlaceName) {
        this.matchingPlaceName = matchingPlaceName;
    }

    public List<Double> getBbox() {
        return bbox;
    }

    public void setBbox(List<Double> bbox) {
        this.bbox = bbox;
    }

}
