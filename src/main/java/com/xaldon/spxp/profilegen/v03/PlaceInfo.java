package com.xaldon.spxp.profilegen.v03;

public class PlaceInfo {
    
    private String profileUri;
    
    private double minLatitude;
    
    private double maxLatitude;
    
    private double minLongitude;
    
    private double maxLongitude;

    public PlaceInfo(String profileUri, double lat1, double long1, double lat2, double long2) {
        this.profileUri = profileUri;
        this.minLatitude = Math.min(lat1, lat2);
        this.maxLatitude = Math.max(lat1,  lat2);
        this.minLongitude = Math.min(long1,  long2);
        this.maxLongitude = Math.max(long1,  long2);
    }

    public String getProfileUri() {
        return profileUri;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }
    
    public double getRandomLatitude(double rand) {
        return minLatitude + (maxLatitude-minLatitude) * rand;
    }
    
    public double getRandomLongitude(double rand) {
        return minLongitude + (maxLongitude-minLongitude) * rand;
    }

}