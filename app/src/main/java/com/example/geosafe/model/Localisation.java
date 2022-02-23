package com.example.geosafe.model;

import com.example.geosafe.utils.Tools;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Localisation {
    private int accuracy,altitude,bearing,bearingAccuracyDegrees,speed,
            speedAccuracyMetersPerSecond,verticalAccuracyMeters;

    private boolean complete,fromMockProvider;
    private String provider;
    private long time,elapsedRealtimeNanos;
    private double latitude,longitude;

    public Localisation() {
    }

    public int getAccuracy() {
        return accuracy;
    }
   public LatLng getLatLng(){
        return new LatLng(this.getLatitude(), this.getLongitude());

   }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
