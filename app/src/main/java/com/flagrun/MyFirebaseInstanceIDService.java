package com.flagrun;

/**
 * Created by user on 4/19/18.
 */

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        //For registration of token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", refreshedToken); // Storing string
        editor.commit();
        //To displaying token on logcat
        Log.d("TOKEN: ", refreshedToken);
    }
}