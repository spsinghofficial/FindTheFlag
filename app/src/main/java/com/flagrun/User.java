package com.flagrun;


import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;


@IgnoreExtraProperties
public class User {

    public String Name;
    public String Phone;
    public String DeviceId;
    public String team;
    public boolean isWinner;
    public boolean hasFlag;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String phone, String deviceId, String team, boolean hasFlag, boolean isWinner) {
        this.Name = name;
        this.Phone = phone;
        this.DeviceId = deviceId;
        this.team = team;
        this.hasFlag = hasFlag;
        this.isWinner = isWinner;

    }

}
