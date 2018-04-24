package com.flagrun;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MyGame extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private static final int LOCATION_REQUEST_CODE = 101;
    Location userCurrentLocation;
    FusedLocationProviderClient mFusedLocationClient;

    //Flag Target to be set
    LatLng target = new LatLng(43.774059578266744,	-79.335527536651);

    Marker myMarker;
    FirebaseDatabase database;
    DatabaseReference root;
    private ChildEventListener mChildEventListener;
    Button pickFlag;

    boolean ifFlagPicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_my_game);
        String latLong = getIntent().getStringExtra("latLong");

        String arr[] = latLong.split(" - ");
        target = new LatLng(Double.parseDouble(arr[0]),Double.parseDouble(arr[1]));

        TextView textView = findViewById(R.id.teamTV);
        textView.setText(UtilityClass.getTeam(this));
        pickFlag = findViewById(R.id.pickFlag);
        pickFlag.setVisibility(View.INVISIBLE);


        pickFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifFlagPicked = true;
                pickFlag.setVisibility(View.INVISIBLE);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        database = FirebaseDatabase.getInstance();
        if(UtilityClass.getTeam(this).equalsIgnoreCase("Team A")){
            root = database.getReference(Preferences.TEAM_A);
        }else{
            root = database.getReference(Preferences.TEAM_B);
        }


       // getCurrentLocation();
    }

    public void onStart() {
        super.onStart();
// Initiating the connection
        googleApiClient.connect();
    }

    public void onStop() {
        super.onStop();
// Disconnecting the connection
        googleApiClient.disconnect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        drawPolygon();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else
                    Toast.makeText(this, "Location permission is denied", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
    }

    private void addMarkers(){
        if(mMap != null){
            mMap.addMarker(new MarkerOptions().position(target)
                    .title("Flag")).setAlpha(6);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(target));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 17.0f));
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarkers();
        getCurrentLocation();
    }

    private double differenceFromTheTarget(float lat_a, float lng_a, float lat_b, float lng_b) {

        Location stp=new Location("myLoac");
        stp.setLatitude(lat_a);
        stp.setLongitude(lng_a);

        Location endp=new Location("end");
        endp.setLatitude(lat_a);
        endp.setLongitude(lng_b);

        int newD=(int)stp.distanceTo(endp);

        return  (float)newD;
    }

    private void getCurrentLocation() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLatLong(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
                userCurrentLocation = location;
                if(myMarker!=null){
                    myMarker.remove();
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are Here").alpha(3);
                    myMarker = mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                }
                userCurrentLocation = location;
                if(ifFlagPicked){
                    if(isUserBackInOwnRegion()){
                        double distance = differenceFromTheTarget((float) userCurrentLocation.getLatitude(), (float)userCurrentLocation.getLongitude(), (float)initialLocation.getLatitude(),(float)initialLocation.getLongitude());
                        if(distance == 0){
                            updateWinnerRecord(true);
                            showMessage("You are a winner");
                            UtilityClass.setUserOutStatus(MyGame.this, 1);
                        Intent intent = new Intent(MyGame.this, FinishGameActivity.class);
                        intent.putExtra("status","You have achieved the target and your team is the winner.");
                        startActivity(intent);
                        MyGame.this.finish();
                        }else
                        {
                            showMessage("You are "+distance+ " KM away from your starting point");
                        }
                    }else{

                    }

                }else{
                    if(isUserInTheRegion()){
                        double distance = differenceFromTheTarget((float) userCurrentLocation.getLatitude(), (float)userCurrentLocation.getLongitude(), (float)target.latitude,(float)target.longitude);

                        Log.d("f", "Hye"+distance);

                        if(distance == 0){
                            pickFlag.setVisibility(View.VISIBLE);
                            updateFlagStatus( true);
                            showMessage("You are reached to the Flag.\nPlease pick up it and go to your initial position");
                        }else
                        {
                            pickFlag.setVisibility(View.INVISIBLE);
                            showMessage("You are "+distance+ " KM away from your target");
                        }
                    }else{
                        UtilityClass.setUserOutStatus(MyGame.this, 1);
                        showMessage("You are in prison\nYou are out of the game");
                        Intent intent = new Intent(MyGame.this, FinishGameActivity.class);
                        intent.putExtra("status","You are in prison\nYou are out of the game");
                        startActivity(intent);
                        MyGame.this.finish();
                    }
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        String locationProvide = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                return;
            }


        }
        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                return;
            }
        }
        locationManager.requestLocationUpdates(locationProvide, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(locationProvide);
        initialLocation = location;
    }
    Location initialLocation;


    public void updateWinnerRecord(final boolean winStatus){
        root.orderByChild("Phone").equalTo(UtilityClass.getPhone(this)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                root.child(dataSnapshot.getKey()).child("isWinner").setValue(winStatus);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("User key", child.getKey());
                    Log.d("User ref", child.getRef().toString());
                    Log.d("User val", child.getValue().toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    public  void updateLatLong( final String latitude, final String longitude) {
        root.orderByChild("Phone").equalTo(UtilityClass.getPhone(this)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                root.child(dataSnapshot.getKey()).child("latitude").setValue(latitude);
                root.child(dataSnapshot.getKey()).child("longitude").setValue(longitude);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("User key", child.getKey());
                    Log.d("User ref", child.getRef().toString());
                    Log.d("User val", child.getValue().toString());
                }
                checkGameStatus();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


    }

    void checkGameStatus(){
                mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Log.d("kshlon","databse " + dataSnapshot.getKey() + "   "+ UtilityClass.getPhone(MyGame.this));
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.Phone.equals(UtilityClass.getPhone(MyGame.this))){
                            boolean status;
                            try{
                                Log.d("kshlon"," xxx "+user.team);
                                status = user.isWinner;

                            }catch (Exception e){
                                e.printStackTrace();
                                status = false;
                            }
                            if( status){
                                String message = "Player " + user.Name  + " of "+ user.team + " has found the flag and is winner of the game" ;
                                sendLocalNotification(message, user.team);


                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        root.addChildEventListener(mChildEventListener);
    }

    void sendLocalNotification(String message, String team){
        Intent intent = new Intent(this, MyGame.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b =  new NotificationCompat.Builder(this, "M_CH_ID");


        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("FindTheFlag")
                .setContentTitle(team +" Won")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }


    public  void updateFlagStatus( final boolean isFlag) {
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                root.child(dataSnapshot.getKey()).child("hasFlag").setValue(isFlag);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        root.addChildEventListener(mChildEventListener);

    }


    private boolean isUserInTheRegion(){
        if(userCurrentLocation!=null && oppositeTeamVertices!=null && oppositeTeamVertices.size() > 0){
            return PolyUtil.containsLocation(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()), oppositeTeamVertices, false);
        }
        return false;
    }

    private boolean isUserBackInOwnRegion(){
        if(userCurrentLocation!=null && ownTeamVertices!=null && ownTeamVertices.size() > 0){
            return PolyUtil.containsLocation(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()), ownTeamVertices, false);
        }
        return false;
    }


    ArrayList<LatLng> oppositeTeamVertices;
    ArrayList<LatLng> ownTeamVertices;

    public void drawPolygon(){
        LatLng torontoPublic = new LatLng(43.7742667825012,-79.33657939414968);
        LatLng missisauga = new LatLng(43.77450693844755, -79.33477694969167);
        LatLng vaughan = new LatLng(43.77333910214474,	-79.33499109484802);

        LatLng torontoCity = new LatLng(43.77321514838842, -79.33562141396652);
        LatLng brampton =  new LatLng(43.7742667825012,-79.33657939414968);

        List<LatLng> list = new ArrayList<>();
        list.add(torontoPublic);
        list.add(missisauga);
        list.add(vaughan);
        list.add(torontoCity);
        list.add(brampton);
        list.add(torontoPublic);

        oppositeTeamVertices = new ArrayList<>();
        oppositeTeamVertices.addAll(list);
        PolygonOptions rectOptions = new PolygonOptions()
                .add(torontoPublic,
                        missisauga,
                        vaughan,torontoCity,brampton, torontoPublic);
        Polygon polygon = mMap.addPolygon(rectOptions);
        polygon.setFillColor(Color.LTGRAY);

        LatLng torontoPublicOwn = new LatLng(43.774444962811806,-79.3355386970519);
        LatLng missisaugaOwn = new LatLng(43.77450693844755, -79.33477694969167);
        LatLng vaughanOwn = new LatLng(43.77333910214474,	-79.33499109484802);
        LatLng torontoCityOwn = new LatLng(43.77331727809993, -79.3353009223938);
        LatLng bramptonOwn = new LatLng(43.774444962811806,-79.3355386970519);

        List<LatLng> listOwn = new ArrayList<>();
        listOwn.add(torontoPublicOwn);
        listOwn.add(missisaugaOwn);
        listOwn.add(vaughanOwn);
        listOwn.add(torontoCityOwn);
        listOwn.add(bramptonOwn);
        listOwn.add(torontoPublicOwn);
        ownTeamVertices = new ArrayList<>();
        ownTeamVertices.addAll(listOwn);

        PolygonOptions rectOptionsOwn = new PolygonOptions()
                .add(torontoPublic,
                        missisauga,
                        vaughan,torontoCity,brampton, torontoPublic);
        Polygon polygonOwn = mMap.addPolygon(rectOptionsOwn);
        polygonOwn.setFillColor(Color.LTGRAY);
    }
}
