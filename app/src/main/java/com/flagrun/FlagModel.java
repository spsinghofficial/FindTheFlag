package com.flagrun;
import com.google.firebase.database.IgnoreExtraProperties;

public class FlagModel {
    public double latitude;
    public double longitude;

    public FlagModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public FlagModel(){}
}
