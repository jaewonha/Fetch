package com.tonyodev.fetchapp;

public class GPSData {
    long ts;
    double latitude, longitude;

    public GPSData(long ts, double latitude, double longitude) {
        this.ts = ts;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
