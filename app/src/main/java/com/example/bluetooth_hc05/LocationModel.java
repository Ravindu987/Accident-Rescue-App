package com.example.bluetooth_hc05;

public class LocationModel {

    String time, longitude, latitude,velocity;

    public LocationModel(String time, String latitude, String longitude, String velocity) {
        this.time = time;
        this.longitude = longitude;
        this.latitude = latitude;
        this.velocity = velocity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getVelocity() {
        return velocity;
    }

    public void setVelocity(String velocity) {
        this.velocity = velocity;
    }
}
